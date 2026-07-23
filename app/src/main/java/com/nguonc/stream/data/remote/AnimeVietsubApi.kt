package com.nguonc.stream.data.remote

import com.nguonc.stream.debug.AppLogger
import com.nguonc.stream.debug.LogTags
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * AnimeVietsubApi — Parse HTML từ animevietsub.wiki
 *
 * Không dùng Retrofit (không có JSON API) — dùng OkHttp + Jsoup.
 *
 * URL Structure:
 *  - /anime-moi/ — anime mới
 *  - /anime-le/ — anime lẻ
 *  - /anime-bo/ — anime bộ
 *  - /tim-kiem/{keyword}/ — tìm kiếm
 *  - /phim/{slug}/ — chi tiết
 *  - /phim/{slug}/tap-{ep}-{id}.html — xem tập
 *  - /{path}/trang-{page}.html — phân trang
 */
class AnimeVietsubApi(
    private val okHttpClient: OkHttpClient,
) {
    companion object {
        const val BASE_URL = "https://animevietsub.wiki"
        private const val UA = "Mozilla/5.0 (Linux; Android 14; Pixel 8) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
    }

    private suspend fun fetchHtml(path: String): Document {
        val url = if (path.startsWith("http")) path else "$BASE_URL$path"
        AppLogger.d(LogTags.API, "→ AVS GET $url")
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", UA)
            .header("Accept", "text/html,application/xhtml+xml")
            .header("Accept-Language", "vi-VN,vi;q=0.9")
            .header("Referer", BASE_URL)
            .build()
        val response = okHttpClient.newCall(request).execute()
        val html = response.body?.string().orEmpty()
        response.close()
        if (html.isBlank()) {
            AppLogger.e(LogTags.API, "AVS: empty response from $url")
            throw Exception("Empty response from animevietsub.wiki")
        }
        return Jsoup.parse(html, BASE_URL)
    }

    // ============================================================
    // ANIME LIST
    // ============================================================

    data class AVSAnime(
        val title: String,
        val slug: String,
        val posterUrl: String,
        val epCurrent: String,
        val score: String,
        val views: String,
        val quality: String,
        val year: String,
    )

    data class AVSPage(
        val items: List<AVSAnime>,
        val currentPage: Int,
        val totalPages: Int,
    )

    suspend fun getList(type: String, page: Int): AVSPage {
        val path = if (page <= 1) "/$type/" else "/$type/trang-$page.html"
        val doc = fetchHtml(path)
        val items = parseAnimeList(doc)
        val (curPage, totalPages) = parsePagination(doc)
        AppLogger.i(LogTags.API, "AVS getList($type, $page) → ${items.size} items, page $curPage/$totalPages")
        return AVSPage(items, curPage, totalPages)
    }

    suspend fun search(keyword: String, page: Int): AVSPage {
        val path = if (page <= 1) "/tim-kiem/$keyword/" else "/tim-kiem/$keyword/trang-$page.html"
        val doc = fetchHtml(path)
        val items = parseAnimeList(doc)
        val (curPage, totalPages) = parsePagination(doc)
        return AVSPage(items, curPage, totalPages)
    }

    private fun parseAnimeList(doc: Document): List<AVSAnime> {
        return doc.select("li.TPostMv article.TPost").map { article ->
            val linkEl = article.selectFirst("a[href]")!!
            val href = linkEl.attr("href")
            val slug = href.substringAfter("/phim/").trimEnd('/')

            val imgEl = article.selectFirst("div.Image img")
            val posterUrl = imgEl?.attr("src") ?: ""
            val title = article.selectFirst("h2.Title")?.text() ?: ""
            val epCurrent = article.selectFirst("span.mli-eps i")?.text() ?: ""
            val score = article.selectFirst("div.anime-avg-user-rating")?.text() ?: ""
            val views = article.selectFirst("span.Year")?.text() ?: ""
            val quality = article.selectFirst("span.Qlty")?.text() ?: ""
            val year = article.selectFirst("span.Date")?.text() ?: ""

            AVSAnime(title, slug, posterUrl, epCurrent, score, views, quality, year)
        }
    }

    private fun parsePagination(doc: Document): Pair<Int, Int> {
        val pagesText = doc.selectFirst("span.pages")?.text() ?: ""
        // "Trang 1 của 198"
        val regex = Regex("Trang (\\d+) của (\\d+)")
        val match = regex.find(pagesText)
        val cur = match?.groupValues?.get(1)?.toIntOrNull() ?: 1
        val total = match?.groupValues?.get(2)?.toIntOrNull() ?: 1
        return cur to total
    }

    // ============================================================
    // DETAIL + EPISODES
    // ============================================================

    data class AVSDetail(
        val title: String,
        val slug: String,
        val posterUrl: String,
        val synopsis: String,
        val genres: List<String>,
        val year: String,
        val studio: String,
        val episodes: List<AVSEpisode>,
    )

    data class AVSEpisode(
        val number: String,
        val url: String,
        val epId: String,
    )

    suspend fun getDetail(slug: String): AVSDetail {
        val doc = fetchHtml("/phim/$slug/")
        val title = doc.selectFirst("h1.Title")?.text()
            ?: doc.selectFirst(".MovieTitle")?.text()
            ?: ""
        val posterUrl = doc.selectFirst(".MovieThumb img")?.attr("src")
            ?: doc.selectFirst("article.TPost img")?.attr("src") ?: ""
        val synopsis = doc.selectFirst(".Description p")?.text() ?: ""
        val genres = doc.select(".Genre a").map { it.text() }
        val year = doc.selectFirst(".Info .Date")?.text() ?: ""
        val studio = doc.selectFirst(".Director span")?.text() ?: ""

        // Parse episodes — look for links with data-play="api"
        val episodes = doc.select("a[data-play=api]").map { el ->
            val href = el.attr("href")
            val number = el.text()
            val epId = href.substringAfterLast("-").substringBefore(".html")
            AVSEpisode(number, href, epId)
        }
        // If no data-play episodes, try regular tap- links
        val altEpisodes = if (episodes.isEmpty()) {
            doc.select("a[href*=tap-]").map { el ->
                val href = el.attr("href")
                val number = el.text()
                val epId = href.substringAfterLast("-").substringBefore(".html")
                AVSEpisode(number, href, epId)
            }
        } else episodes

        AppLogger.i(LogTags.API, "AVS getDetail($slug) → ${altEpisodes.size} episodes")
        return AVSDetail(title, slug, posterUrl, synopsis, genres, year, studio, altEpisodes)
    }

    // ============================================================
    // PLAYER — extract iframe URL
    // ============================================================

    data class AVSPlayer(
        val iframeUrl: String,
        val filmId: String,
        val episodeId: String,
    )

    suspend fun getPlayer(episodeUrl: String): AVSPlayer {
        val doc = fetchHtml(episodeUrl)
        // Find iframe with stream.googleapiscdn.com
        val iframe = doc.selectFirst("iframe[src*=stream.googleapiscdn]")
            ?: doc.selectFirst("iframe[src*=player]")
            ?: doc.selectFirst("#media-player iframe")
        val iframeUrl = iframe?.attr("src") ?: ""
        // Parse filmInfo from JS
        val html = doc.html()
        val filmIdRegex = Regex("filmInfo\\.filmID\\s*=\\s*parseInt\\('(\\d+)'\\)")
        val epIdRegex = Regex("filmInfo\\.episodeID\\s*=\\s*parseInt\\('(\\d+)'\\)")
        val filmId = filmIdRegex.find(html)?.groupValues?.get(1) ?: ""
        val episodeId = epIdRegex.find(html)?.groupValues?.get(1) ?: ""

        AppLogger.i(LogTags.API, "AVS getPlayer → iframe=${iframeUrl.take(80)}, filmId=$filmId, epId=$episodeId")
        return AVSPlayer(iframeUrl, filmId, episodeId)
    }
}
