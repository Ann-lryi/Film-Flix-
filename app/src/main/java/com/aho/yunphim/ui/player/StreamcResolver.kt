package com.aho.yunphim.ui.player

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

/**
 * Cổng phát video cho embed streamc.xyz (và domain CDN cùng họ) của phim.nguonc.com.
 *
 * Port ban đầu từ luồng "NEW WORKING FLOW" reverse-engineer 06/2026 trong plugin CloudStream
 * NguonC của Aho (B1 fetch HTML tìm token trong data-obf -> B2 POST lấy xat -> B3 GET
 * {token}.m3u9?xat=... -> B4 proxy cục bộ). [resolve] vẫn giữ luồng này làm thử trước (rẻ, có
 * thể vẫn còn đúng với 1 số embed domain chưa đổi).
 *
 * XÁC NHẬN THẬT qua StreamBrowser 18/07/2026 (Aho tự cào): site ĐÃ đổi cơ chế - `data-obf` không
 * còn trong HTML, token giờ do JS obfuscated (player.js) tự dựng lúc chạy. URL playlist thật KHÔNG
 * còn đuôi .m3u8/.m3u9, mà là 1 chuỗi base64 dài (`eyJoIjoi...`, giải mã ra chứa field "h" = hash
 * embed) + `?xat=...`. Vì token cần chạy JS thật mới dựng được, [resolve] (HTTP thuần) nhiều khả
 * năng sẽ fail ở bước "extract_token" cho các embed domain đã đổi - đây là lý do có
 * [finalizeFromKnownUrl]: WebViewStreamResolver chạy JS thật, tự bắt được URL đã xác thực (có
 * `xat=`), rồi gọi hàm này để tái dùng đúng hạ tầng proxy cục bộ (Referer + tẩy đuôi .png) mà
 * không cần tự dựng token.
 *
 * KHÔNG PORT: nhánh giải mã AES-GCM cũ (kX/data-obf/token dò nhiều key ứng viên) - dữ liệu thật
 * thu được (18/07/2026) xác nhận playlist hiện tại KHÔNG mã hoá (#EXTM3U sạch), nên vẫn chưa cần.
 *
 * Mỗi bước đều Log.d/Log.e với tag "StreamcResolver" - lọc Logcat theo tag này để xem chính xác
 * bước nào fail và nội dung response thật (kèm OkHttp BODY logging đã bật sẵn ở AppContainer).
 */
class StreamcResolver(private val httpClient: OkHttpClient) {

    companion object {
        private const val TAG = "StreamcResolver"

        private val FAMILY_REGEX = Regex(
            """streamc\.xyz|amass\d+\.top|hihihoho\d+\.top|phimmoi\.net|seouls\d+\.amass\d+\.top""",
        )

        /** true nếu URL thuộc họ domain streamc.xyz - cần luồng token thay vì phát thẳng. */
        fun isStreamcFamily(url: String): Boolean = FAMILY_REGEX.containsMatchIn(url)
    }

    /** Kết quả có bước fail cụ thể - PlayerViewModel hiện đúng bước lỗi lên UI, không còn null chung chung. */
    sealed interface Result {
        data class Success(val proxyUrl: String) : Result
        data class Failure(val step: String, val detail: String) : Result
    }

    @Serializable
    private data class AccessResponse(val ok: Boolean? = null, val xat: String? = null)

    private val json = Json { ignoreUnknownKeys = true }
    private val activeProxies = CopyOnWriteArrayList<LocalM3u8Proxy>()

    suspend fun resolve(embedUrl: String): Result = withContext(Dispatchers.IO) {
        try {
            val fixedUrl = fixKnownDeadHost(embedUrl)
            val embedDomain = Regex("""(https?://embed\d+\.streamc\.xyz)""").find(fixedUrl)?.groupValues?.getOrNull(1)
                ?: Regex("""(https?://[^/]+)""").find(fixedUrl)?.groupValues?.getOrNull(1)
                ?: return@withContext fail("domain", "Không tách được domain từ URL: $fixedUrl")

            Log.d(TAG, "B1 fetch HTML: $fixedUrl (domain=$embedDomain)")
            val html = fetchHtml(fixedUrl)
                ?: return@withContext fail("fetch_html", "HTTP request tới $fixedUrl không thành công hoặc rỗng")
            Log.d(TAG, "B1 OK, HTML dài ${html.length} ký tự")

            val token = extractToken(html)
                ?: return@withContext fail(
                    "extract_token",
                    "Không tìm thấy data-obf/sUb hay pattern token nào trong HTML - token giờ có " +
                        "thể do JS dựng lúc chạy (site đã bỏ data-obf tĩnh), cần WebView chạy JS " +
                        "thật để dò (xem PlayerViewModel fallback qua WebViewStreamResolver).",
                )
            Log.d(TAG, "B1 tìm được token: $token")

            Log.d(TAG, "B2 POST xác thực: $embedDomain/$token")
            val xat = confirmAccess(embedDomain, token, fixedUrl)
                ?: return@withContext fail(
                    "confirm_access",
                    "POST $embedDomain/$token không trả ok=true+xat - site có thể đổi cơ chế xác thực",
                )
            Log.d(TAG, "B2 OK, xat=$xat")

            Log.d(TAG, "B3 GET playlist với xat")
            val playlist = fetchPlaylist(embedDomain, token, xat, fixedUrl)
                ?: return@withContext fail(
                    "fetch_playlist",
                    "Cả .m3u9 và .m3u8 đều không trả playlist hợp lệ (thiếu #EXTM3U hoặc còn #ENC-AESGCM)",
                )
            Log.d(TAG, "B3 OK, playlist dài ${playlist.length} ký tự")
            buildProxyResult(playlist, fixedUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Exception không dự kiến trong luồng resolve", e)
            fail("exception", "${e::class.simpleName}: ${e.message}")
        }
    }

    /**
     * Dùng khi ĐÃ CÓ URL playlist thật (vd. WebViewStreamResolver dò được bằng cách chạy JS thật
     * và bắt request tự nhiên của trang - cần thiết vì token hiện do JS obfuscated dựng lúc chạy,
     * không còn nằm tĩnh trong HTML để [resolve] tự dựng qua HTTP thuần được nữa). Vẫn fetch lại
     * bằng OkHttp (không dùng thẳng nội dung WebView đã tải) để bảo đảm header/cookie nhất quán,
     * rồi tái dùng đúng hạ tầng proxy cục bộ đã có (Referer + tẩy đuôi .png cho segment).
     */
    suspend fun finalizeFromKnownUrl(playlistUrl: String, refererUrl: String): Result = withContext(Dispatchers.IO) {
        try {
            val embedDomain = Regex("""(https?://[^/]+)""").find(playlistUrl)?.groupValues?.getOrNull(1)
                ?: return@withContext fail("domain", "Không tách được domain từ URL đã dò: $playlistUrl")
            Log.d(TAG, "Fetch trực tiếp URL đã dò từ WebView: $playlistUrl")
            val playlist = fetchPlaylistOnce(playlistUrl, refererUrl, embedDomain)
                ?: return@withContext fail("fetch_playlist", "URL WebView dò được không fetch lại được qua OkHttp")
            if (!playlist.contains("#EXTM3U")) {
                return@withContext fail("fetch_playlist", "Response không phải M3U8 hợp lệ (thiếu #EXTM3U)")
            }
            if (playlist.contains("#ENC-AESGCM")) {
                return@withContext fail("fetch_playlist", "Playlist bị mã hoá AES-GCM - chưa hỗ trợ giải mã")
            }
            buildProxyResult(playlist, refererUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Exception trong finalizeFromKnownUrl", e)
            fail("exception", "${e::class.simpleName}: ${e.message}")
        }
    }

    private fun buildProxyResult(playlist: String, refererUrl: String): Result {
        val proxy = LocalM3u8Proxy(referer = refererUrl, httpClient = httpClient)
        proxy.start()
        activeProxies.add(proxy)
        proxy.setPlaylist(rewriteSegmentUrls(playlist, proxy.base))
        Log.d(TAG, "Proxy cục bộ sẵn sàng: ${proxy.base}")
        return Result.Success("${proxy.base}/stream.m3u8")
    }

    private fun fail(step: String, detail: String): Result {
        Log.e(TAG, "FAIL ở bước '$step': $detail")
        return Result.Failure(step, detail)
    }

    /** Gọi khi rời màn hình player / đổi tập - dừng hết proxy cục bộ đang chạy nền. */
    fun stopActiveProxies() {
        activeProxies.forEach { it.stop() }
        activeProxies.clear()
    }

    /** sing.phimmoi.net là domain đã chết theo ghi chú trong plugin gốc - quy đổi về embed15.streamc.xyz. */
    private fun fixKnownDeadHost(url: String): String {
        if (!url.contains("sing.phimmoi.net")) return url
        val hash = Regex("""/([^/]+)/hls\.m3u8""").find(url)?.groupValues?.getOrNull(1) ?: return url
        return "https://embed15.streamc.xyz/embed.php?hash=$hash"
    }

    private fun fetchHtml(url: String): String? {
        val request = Request.Builder().url(url)
            .header("Referer", "https://phim.nguonc.com/")
            .build()
        return httpClient.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) null else resp.body?.string()
        }
    }

    /** Ưu tiên field "sUb" trong data-obf (base64 JSON) - fallback dò token trực tiếp trong HTML. */
    private fun extractToken(html: String): String? {
        val obfRaw = Regex("""data-obf=["']([^"']+)["']""").find(html)?.groupValues?.getOrNull(1)
        if (obfRaw != null) {
            for (decoded in decodeBase64Variants(obfRaw)) {
                val sub = Regex(""""sUb"\s*:\s*"([^"]+)"""").find(decoded)?.groupValues?.getOrNull(1)
                if (!sub.isNullOrBlank()) return sub
            }
        }
        val jwPlayerFile = Regex(""""file"\s*:\s*"([A-Za-z0-9_+/=-]+)\.(?:m3u8|m3u9)"""").find(html)
        if (jwPlayerFile != null) return jwPlayerFile.groupValues[1]
        val anyTokenPath = Regex("""/([A-Za-z0-9_+/=-]{20,})\.(?:m3u8|m3u9)""").find(html)
        return anyTokenPath?.groupValues?.getOrNull(1)
    }

    private fun decodeBase64Variants(raw: String): List<String> {
        val results = mutableListOf<String>()
        val flagSets = listOf(Base64.DEFAULT, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        for (flags in flagSets) {
            try {
                results.add(String(Base64.decode(raw, flags), Charsets.UTF_8))
            } catch (_: Exception) {
                // thử flag tiếp theo
            }
        }
        return results
    }

    /** B2: POST /{token} -> {"ok":true,"xat":"..."}. */
    private fun confirmAccess(embedDomain: String, token: String, refererUrl: String): String? {
        val request = Request.Builder()
            .url("$embedDomain/$token")
            .header("Referer", refererUrl)
            .header("Origin", embedDomain)
            .header("Accept", "application/json, text/plain, */*")
            .post("".toRequestBody("application/json".toMediaType()))
            .build()
        return httpClient.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            runCatching { json.decodeFromString<AccessResponse>(body) }.getOrNull()?.xat
        }
    }

    /** B3: thử .m3u9 (mobile, không mã hoá) trước, .m3u8 (desktop) sau nếu .m3u9 fail. */
    private fun fetchPlaylist(embedDomain: String, token: String, xat: String, refererUrl: String): String? {
        for (ext in listOf("m3u9", "m3u8")) {
            val url = "$embedDomain/$token.$ext?xat=$xat"
            val content = fetchPlaylistOnce(url, refererUrl, embedDomain) ?: continue
            if (content.contains("#EXTM3U") && !content.contains("#ENC-AESGCM")) return content
        }
        return null
    }

    private fun fetchPlaylistOnce(url: String, refererUrl: String, embedDomain: String): String? {
        val request = Request.Builder().url(url)
            .header("Referer", refererUrl)
            .header("Origin", embedDomain)
            .header("Accept", "*/*")
            .build()
        return httpClient.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) null else resp.body?.string()
        }
    }

    private fun rewriteSegmentUrls(m3u8: String, proxyBase: String): String {
        return m3u8.lines().joinToString("\n") { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("http")) {
                "$proxyBase/seg/${URLEncoder.encode(trimmed, "UTF-8")}"
            } else {
                line
            }
        }
    }
}

/** Proxy HTTP cục bộ trên 127.0.0.1 - phát lại playlist + fetch từng segment kèm đúng Referer. */
private class LocalM3u8Proxy(
    private val referer: String,
    private val httpClient: OkHttpClient,
) {
    private var serverSocket: ServerSocket? = null
    private val threadPool = Executors.newCachedThreadPool()

    @Volatile private var playlist: String = ""

    val base: String get() = "http://127.0.0.1:${serverSocket?.localPort ?: 0}"

    fun setPlaylist(content: String) {
        playlist = content
    }

    fun start() {
        val socket = ServerSocket(0)
        serverSocket = socket
        Thread {
            while (!socket.isClosed) {
                try {
                    val client = socket.accept()
                    threadPool.execute { handle(client) }
                } catch (_: Exception) {
                    break
                }
            }
        }.apply { isDaemon = true }.start()
    }

    private fun handle(client: Socket) {
        try {
            val input = client.getInputStream().bufferedReader()
            val output = client.getOutputStream()
            val requestLine = input.readLine() ?: return
            while (true) {
                val line = input.readLine() ?: break
                if (line.isBlank()) break
            }
            val path = requestLine.split(" ").getOrNull(1) ?: "/"
            val crlf = "\r\n"
            when {
                path == "/stream.m3u8" -> {
                    val body = playlist.toByteArray(Charsets.UTF_8)
                    output.write(
                        (
                            "HTTP/1.1 200 OK$crlf" +
                                "Content-Type: application/vnd.apple.mpegurl$crlf" +
                                "Content-Length: ${body.size}$crlf" +
                                "Access-Control-Allow-Origin: *$crlf$crlf"
                            ).toByteArray(),
                    )
                    output.write(body)
                }

                path.startsWith("/seg/") -> {
                    val segUrl = URLDecoder.decode(path.removePrefix("/seg/"), "UTF-8")
                    try {
                        val request = Request.Builder().url(segUrl).header("Referer", referer).build()
                        httpClient.newCall(request).execute().use { resp ->
                            val bytes = resp.body?.bytes() ?: ByteArray(0)
                            output.write(
                                (
                                    "HTTP/1.1 200 OK$crlf" +
                                        "Content-Type: video/mp2t$crlf" +
                                        "Content-Length: ${bytes.size}$crlf" +
                                        "Access-Control-Allow-Origin: *$crlf$crlf"
                                    ).toByteArray(),
                            )
                            output.write(bytes)
                        }
                    } catch (_: Exception) {
                        output.write("HTTP/1.1 502 Bad Gateway$crlf$crlf".toByteArray())
                    }
                }

                else -> output.write("HTTP/1.1 404 Not Found$crlf$crlf".toByteArray())
            }
            output.flush()
            client.close()
        } catch (_: Exception) {
            try {
                client.close()
            } catch (_: Exception) {
                // đã đóng rồi, bỏ qua
            }
        }
    }

    fun stop() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {
            // đã đóng rồi, bỏ qua
        }
        try {
            threadPool.shutdownNow()
        } catch (_: Exception) {
            // đã tắt rồi, bỏ qua
        }
    }
}
