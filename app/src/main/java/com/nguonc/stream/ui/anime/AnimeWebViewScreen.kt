package com.nguonc.stream.ui.anime

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nguonc.stream.debug.AppLogger
import com.nguonc.stream.debug.LogTags
import com.nguonc.stream.ui.theme.AppShapes
import com.nguonc.stream.ui.theme.FilmFlixIcons
import com.nguonc.stream.ui.theme.Primary
import org.jsoup.Jsoup

/**
 * AnimeWebViewLoader — dùng WebView (Chromium engine) để bypass Cloudflare.
 *
 * Cloudflare chặn OkHttp/Retrofit (server-side requests) nhưng KHÔNG chặn
 * WebView (trình duyệt thật). WebView pass Cloudflare challenge tự động.
 *
 * Flow:
 * 1. WebView load animevietsub.wiki/anime-moi/
 * 2. Cloudflare challenge → WebView tự giải (Chromium engine)
 * 3. Sau khi pass → page loaded → inject JS extract HTML
 * 4. JS interface callback → parse HTML bằng Jsoup → return anime list
 */

data class AnimeParseResult(
    val items: List<AnimeItemData>,
    val currentPage: Int,
    val totalPages: Int,
)

data class AnimeItemData(
    val title: String,
    val slug: String,
    val posterUrl: String,
    val epCurrent: String,
    val score: String,
    val year: String,
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AnimeWebViewLoader(
    url: String,
    onResult: (AnimeParseResult) -> Unit,
    onError: (String) -> Unit,
) {
    var loaded by remember { mutableStateOf(false) }
    var pageHtml by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (!loaded) {
            CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp))
        }

        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            AppLogger.i(LogTags.API, "AVS WebView page finished: $url")
                            // Inject JS to get page HTML
                            view?.evaluateJavascript("""
                                (function() {
                                    try {
                                        var html = document.documentElement.outerHTML;
                                        AndroidInterface.onHtmlReceived(html);
                                    } catch(e) {
                                        AndroidInterface.onError(e.toString());
                                    }
                                })();
                            """.trimIndent(), null)
                        }
                    }

                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onHtmlReceived(html: String) {
                            AppLogger.i(LogTags.API, "AVS WebView HTML received: ${html.length} chars")
                            pageHtml = html
                            loaded = true
                            // Parse with Jsoup
                            try {
                                val doc = Jsoup.parse(html, "https://animevietsub.wiki")
                                val items = doc.select("li.TPostMv article.TPost").map { article ->
                                    val linkEl = article.selectFirst("a[href]")
                                    val href = linkEl?.attr("abs:href") ?: ""
                                    val slug = if (href.contains("/phim/")) {
                                        href.substringAfter("/phim/").trimEnd('/').substringBefore("/")
                                    } else ""
                                    val imgEl = article.selectFirst("div.Image img")
                                    val posterUrl = imgEl?.attr("abs:src") ?: imgEl?.attr("src") ?: ""
                                    val title = article.selectFirst("h2.Title")?.text() ?: ""
                                    val epCurrent = article.selectFirst("span.mli-eps i")?.text() ?: ""
                                    val score = article.selectFirst("div.anime-avg-user-rating")?.text()?.replace("★", "")?.trim() ?: ""
                                    val year = article.selectFirst("span.Date")?.text() ?: ""
                                    AnimeItemData(title, slug, posterUrl, epCurrent, score, year)
                                }.filter { it.title.isNotBlank() }

                                val pagesText = doc.selectFirst("span.pages")?.text() ?: ""
                                val regex = Regex("(\\d+) của (\\d+)")
                                val match = regex.find(pagesText)
                                val curPage = match?.groupValues?.get(1)?.toIntOrNull() ?: 1
                                val totalPages = match?.groupValues?.get(2)?.toIntOrNull() ?: 1

                                AppLogger.i(LogTags.API, "AVS parsed: ${items.size} items, page $curPage/$totalPages")
                                if (items.isNotEmpty()) {
                                    onResult(AnimeParseResult(items, curPage, totalPages))
                                } else {
                                    // If no items, maybe still on Cloudflare page
                                    if (html.contains("Xác Minh") || html.contains("captcha")) {
                                        onError("Cloudflare challenge chưa pass — thử lại")
                                    } else {
                                        onError("Không tìm thấy anime (0 items)")
                                    }
                                }
                            } catch (e: Exception) {
                                AppLogger.e(LogTags.API, "AVS parse error: ${e.message}", e)
                                onError("Lỗi parse: ${e.message}")
                            }
                        }

                        @JavascriptInterface
                        fun onError(error: String) {
                            AppLogger.e(LogTags.API, "AVS WebView JS error: $error")
                            onError(error)
                        }
                    }, "AndroidInterface")

                    AppLogger.i(LogTags.API, "AVS WebView loading: $url")
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Anime Tab Screen — dùng WebView để load animevietsub.wiki
 */
@Composable
fun AnimeWebViewScreen(
    onAnimeClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    var currentUrl by remember { mutableStateOf("https://animevietsub.wiki/anime-moi/") }
    var result by remember { mutableStateOf<AnimeParseResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loadingSection by remember { mutableStateOf("anime-moi") }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Anime", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("anime-moi" to "Mới", "anime-bo" to "Bộ", "anime-le" to "Lẻ").forEach { (type, label) ->
                    Surface(
                        shape = AppShapes.Pill,
                        color = if (loadingSection == type) Primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            loadingSection = type
                            currentUrl = "https://animevietsub.wiki/$type/"
                            result = null
                            error = null
                        }
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (loadingSection == type) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                error != null -> Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        shape = AppShapes.Small, color = Primary,
                        modifier = Modifier.clickable {
                            error = null
                            result = null
                            // Force reload by changing URL slightly
                            currentUrl = "https://animevietsub.wiki/$loadingSection/?t=${System.currentTimeMillis()}"
                        }
                    ) {
                        Text("Thử lại", color = Color.White, modifier = Modifier.padding(16.dp, 8.dp))
                    }
                }
                result == null -> AnimeWebViewLoader(
                    url = currentUrl,
                    onResult = { result = it },
                    onError = { error = it },
                )
                else -> {
                    // Show anime grid
                    val items = result!!.items
                    LazyVerticalGridCompat(items = items, onAnimeClick = onAnimeClick)
                }
            }
        }
    }
}

@Composable
private fun LazyVerticalGridCompat(
    items: List<AnimeItemData>,
    onAnimeClick: (String) -> Unit,
) {
    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
        columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 130.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items.size) { index ->
            val anime = items[index]
            Column(
                modifier = Modifier.clickable { onAnimeClick(anime.slug) }
            ) {
                androidx.compose.material3.Card(
                    shape = AppShapes.Large,
                    modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)
                ) {
                    Box {
                        coil.compose.AsyncImage(
                            model = anime.posterUrl,
                            contentDescription = anime.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    anime.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
            }
        }
    }
}
