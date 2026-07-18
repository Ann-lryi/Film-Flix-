package com.aho.yunphim.ui.player

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Dò link phát trực tiếp bằng WebView thật (chạy JS thật) khi link từ API không phát thẳng
 * được, hoặc khi StreamcResolver.resolve() (HTTP thuần) fail vì token giờ do JS obfuscated dựng
 * lúc chạy (xác nhận qua StreamBrowser 18/07/2026 - data-obf không còn trong HTML nữa).
 *
 * shouldInterceptRequest bắt mọi request của WebView, tìm URL khớp mẫu media. XÁC NHẬN THẬT
 * 18/07/2026: URL playlist thật của streamc.xyz KHÔNG còn đuôi .m3u8/.m3u9 (base64 token dài +
 * `?xat=...`) - regex ban đầu chỉ bắt theo đuôi file sẽ bỏ sót URL dạng này, đã bổ sung nhận diện
 * theo tham số `xat=`. Khi bắt được URL, gọi [StreamcResolver.finalizeFromKnownUrl] để tái dùng
 * đúng hạ tầng proxy cục bộ (Referer + tẩy đuôi .png cho segment) thay vì trả thẳng URL thô cho
 * ExoPlayer (nhiều khả năng vẫn fail vì thiếu Referer đúng).
 *
 * shouldInterceptRequest chạy trên thread nền của WebView (không phải main thread) - mọi thao
 * tác đụng tới WebView instance (stopLoading/destroy) đều phải post về main thread qua Handler,
 * gọi thẳng từ thread nền sẽ vi phạm hợp đồng luồng của WebView. Gọi runBlocking cho
 * finalizeFromKnownUrl an toàn ở đây vì đang ở thread nền (không phải main), không block UI.
 */
object WebViewStreamResolver {

    private val mediaUrlRegex = Regex(
        // .m3u9 là đuôi playlist "mobile" riêng của streamc.xyz (xác nhận qua plugin CloudStream
        // NguonC thật) - regex ban đầu chỉ bắt .m3u8/.mp4, bỏ sót đuôi này.
        pattern = """https?://[^\s"'<>]+\.(m3u8|m3u9|mp4)(\?[^\s"'<>]*)?""",
        option = RegexOption.IGNORE_CASE,
    )

    private val mainHandler = Handler(Looper.getMainLooper())

    private fun isCandidateMediaUrl(url: String): Boolean {
        // `xat=` xác nhận thật 18/07/2026: tham số xác thực của streamc.xyz, URL playlist hiện
        // tại không còn đuôi file để dựa vào regex đuôi file như trước.
        return mediaUrlRegex.containsMatchIn(url) || url.contains("xat=")
    }

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun resolve(
        context: Context,
        embedUrl: String,
        streamcResolver: StreamcResolver,
        timeoutMs: Long = 15_000,
    ): String? {
        return withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { continuation ->
                var webView: WebView? = null

                fun finish(result: String?) {
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                    mainHandler.post {
                        webView?.stopLoading()
                        webView?.destroy()
                        webView = null
                    }
                }

                val wv = WebView(context.applicationContext).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    // Bỏ hậu tố "; wv" khỏi User-Agent mặc định của WebView - một số trang chặn
                    // riêng request có dấu hiệu tới từ WebView nhúng thay vì trình duyệt thật.
                    settings.userAgentString = settings.userAgentString.replace("; wv", "")
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                }

                wv.webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest,
                    ): WebResourceResponse? {
                        val url = request.url.toString()
                        if (isCandidateMediaUrl(url)) {
                            // Đang ở thread nền của WebView (không phải main) - runBlocking an
                            // toàn ở đây, không chặn UI.
                            val proxied = runBlocking {
                                when (val result = streamcResolver.finalizeFromKnownUrl(url, embedUrl)) {
                                    is StreamcResolver.Result.Success -> result.proxyUrl
                                    is StreamcResolver.Result.Failure -> null
                                }
                            }
                            // Nếu proxy hoá thất bại, vẫn thử URL gốc như phương án cuối cùng -
                            // có thể ExoPlayer phát được thẳng nếu site không đòi Referer.
                            finish(proxied ?: url)
                        }
                        return super.shouldInterceptRequest(view, request)
                    }
                }

                webView = wv
                continuation.invokeOnCancellation { finish(null) }
                wv.loadUrl(embedUrl)
            }
        }
    }
}
