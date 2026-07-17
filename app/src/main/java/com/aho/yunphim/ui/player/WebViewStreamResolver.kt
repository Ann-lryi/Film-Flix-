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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Dò link phát trực tiếp bằng WebView ẩn khi link_m3u8 từ API không phát thẳng được. Theo báo
 * cáo thực tế trên forum, họ API-family này (ophim/nguonc) nhiều trường hợp chỉ link_embed dùng
 * được, HLS trực tiếp không phát - tái dùng đúng kỹ thuật đã chứng minh hiệu quả ở StreamBrowser:
 * shouldInterceptRequest bắt mọi request của WebView, tìm URL khớp mẫu media (.m3u8/.mp4).
 *
 * KHÔNG ĐỦ DỮ LIỆU ĐỂ XÁC MINH: chưa kiểm thử được trên phim.nguonc.com cụ thể (không có môi
 * trường Android thật ở đây). Logic dựa trên pattern chung đã hoạt động với nhiều trang embed
 * video tiếng Việt - cần Aho xác nhận lại khi build thật trên Honor GT Pro.
 *
 * shouldInterceptRequest chạy trên thread nền của WebView (không phải main thread) - mọi thao
 * tác đụng tới WebView instance (stopLoading/destroy) đều phải post về main thread qua Handler,
 * gọi thẳng từ thread nền sẽ vi phạm hợp đồng luồng của WebView.
 */
object WebViewStreamResolver {

    private val mediaUrlRegex = Regex(
        // .m3u9 là đuôi playlist "mobile" riêng của streamc.xyz (xác nhận qua plugin CloudStream
        // NguonC thật) - regex ban đầu chỉ bắt .m3u8/.mp4, bỏ sót đuôi này.
        pattern = """https?://[^\s"'<>]+\.(m3u8|m3u9|mp4)(\?[^\s"'<>]*)?""",
        option = RegexOption.IGNORE_CASE,
    )

    private val mainHandler = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun resolve(context: Context, embedUrl: String, timeoutMs: Long = 15_000): String? {
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
                        if (mediaUrlRegex.containsMatchIn(url)) {
                            finish(url)
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
