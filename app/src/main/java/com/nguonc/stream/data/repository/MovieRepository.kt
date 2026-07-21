package com.nguonc.stream.data.repository

import com.nguonc.stream.data.local.FavoriteDao
import com.nguonc.stream.data.local.FavoriteEntity
import com.nguonc.stream.data.local.HistoryDao
import com.nguonc.stream.data.local.HistoryEntity
import com.nguonc.stream.data.remote.NguoncApi
import com.nguonc.stream.data.remote.PhimApi
import com.nguonc.stream.data.remote.dto.CategoryDto
import com.nguonc.stream.data.remote.dto.EpisodeDto
import com.nguonc.stream.data.remote.dto.EpisodeServerDto
import com.nguonc.stream.data.remote.dto.MovieDetailDto
import com.nguonc.stream.debug.AppLogger
import com.nguonc.stream.debug.LogTags
import okhttp3.OkHttpClient
import com.nguonc.stream.data.remote.dto.MovieItemDto
import com.nguonc.stream.data.remote.dto.NguoncMovieDetailDto
import com.nguonc.stream.data.remote.dto.NguoncMovieItemDto
import com.nguonc.stream.data.remote.dto.PaginationDto
import com.nguonc.stream.data.remote.dto.TmdbDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/** Kết quả 1 trang danh sách phim */
data class MoviePage(
    val items: List<MovieItemDto>,
    val pagination: PaginationDto,
)

/** Chi tiết phim kèm danh sách server/tập */
data class MovieDetailBundle(
    val movie: MovieDetailDto,
    val episodes: List<EpisodeServerDto>,
)

@Singleton
class MovieRepository @Inject constructor(
    private val api: PhimApi,
    private val nguoncApi: NguoncApi,
    private val okHttpClient: OkHttpClient,
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao,
) {
    // ============================================================
    // REMOTE — List endpoints (dùng NguoncApi = "API gốc")
    // ============================================================

    /**
     * Lấy danh sách phim theo loại.
     * Mapping sang NguoncApi:
     *  - "phim-moi-cap-nhat" → /films/phim-moi-cap-nhat
     *  - "phim-le" / "phim-bo" → /films/danh-sach/{type}
     *  - "hoat-hinh" → /films/the-loai/hoat-hinh (đặc biệt: dùng endpoint the-loai)
     *  - "tv-shows" → /films/danh-sach/tv-shows (giả định tương tự)
     */
    suspend fun getMovieList(type: String, page: Int): MoviePage = io {
        AppLogger.d(LogTags.REPO, "getMovieList(type=$type, page=$page) — calling NguoncApi...")
        try {
            val res = when (type) {
                "phim-moi-cap-nhat" -> {
                    AppLogger.d(LogTags.API, "→ GET /films/phim-moi-cap-nhat?page=$page")
                    nguoncApi.getPhimMoiCapNhat(page)
                }
                "phim-le", "phim-bo", "tv-shows" -> {
                    AppLogger.d(LogTags.API, "→ GET /films/danh-sach/$type?page=$page")
                    nguoncApi.getDanhSach(type, page)
                }
                "hoat-hinh" -> {
                    AppLogger.d(LogTags.API, "→ GET /films/the-loai/hoat-hinh?page=$page (special endpoint)")
                    nguoncApi.getTheLoai("hoat-hinh", page)
                }
                else -> {
                    AppLogger.w(LogTags.REPO, "Unknown type '$type', falling back to /films/danh-sach/$type")
                    nguoncApi.getDanhSach(type, page)
                }
            }
            AppLogger.success(
                LogTags.REPO,
                "getMovieList($type, page=$page) ✓ — got ${res.items.size} items, status=${res.status}, totalPage=${res.paginate.totalPage}"
            )
            MoviePage(
                items = res.items.map { it.toMovieItemDto() },
                pagination = PaginationDto(
                    totalItems = res.paginate.totalItems,
                    totalItemsPerPage = res.paginate.itemsPerPage,
                    currentPage = res.paginate.currentPage,
                    totalPages = res.paginate.totalPage,
                ),
            )
        } catch (e: Exception) {
            AppLogger.e(LogTags.REPO, "getMovieList($type, page=$page) FAILED: ${e.message}", e)
            throw e
        }
    }

    /** Tìm kiếm — dùng NguoncApi */
    suspend fun search(keyword: String, page: Int): MoviePage = io {
        AppLogger.d(LogTags.API, "→ GET /films/search?keyword=$keyword&page=$page")
        try {
            val res = nguoncApi.search(keyword, page)
            AppLogger.success(
                LogTags.REPO,
                "search('$keyword', page=$page) ✓ — got ${res.items.size} results"
            )
            MoviePage(
                items = res.items.map { it.toMovieItemDto() },
                pagination = PaginationDto(
                    totalItems = res.paginate.totalItems,
                    totalItemsPerPage = res.paginate.itemsPerPage,
                    currentPage = res.paginate.currentPage,
                    totalPages = res.paginate.totalPage,
                ),
            )
        } catch (e: Exception) {
            AppLogger.e(LogTags.REPO, "search('$keyword', page=$page) FAILED: ${e.message}", e)
            throw e
        }
    }

    // ============================================================
    // REMOTE — Browse (Categories/Countries) — vẫn dùng PhimApi
    // (phimapi.com có endpoint /the-loai và /quoc-gia riêng)
    // ============================================================

    suspend fun getByCategory(slug: String, page: Int): MoviePage = io {
        val res = api.getByCategory(slug, page)
        val cdn = res.data.cdnImage
        MoviePage(
            items = res.data.items.map { it.normalizeImage(cdn) },
            pagination = res.data.params.pagination,
        )
    }

    suspend fun getByCountry(slug: String, page: Int): MoviePage = io {
        val res = api.getByCountry(slug, page)
        MoviePage(
            items = res.data.items.map { it.normalizeImage(res.data.cdnImage) },
            pagination = res.data.params.pagination,
        )
    }

    // ============================================================
    // REMOTE — Detail (dùng NguoncApi)
    // ============================================================

    suspend fun getMovieDetail(slug: String): MovieDetailBundle = io {
        AppLogger.d(LogTags.API, "→ GET /film/$slug (detail)")
        try {
            val res = nguoncApi.getFilmDetail(slug)
            val movie = res.movie.toMovieDetailDto()
            AppLogger.i(
                LogTags.REPO,
                "Detail loaded: \"${movie.name}\" — ${res.movie.episodes.size} raw servers"
            )
            res.movie.episodes.forEachIndexed { idx, srv ->
                AppLogger.d(
                    LogTags.REPO,
                    "  raw server #${idx + 1}: name=\"${srv.serverName}\", ${srv.items.size} episodes"
                )
            }
            // ⚡ LAZY LOAD: KHÔNG extract m3u8 cho tất cả tập ở đây!
            // Chỉ lưu embed URL → khi user bấm Play mới gọi extractM3u8ForEpisode().
            // Trước đây: 18 tập × ~100ms = ~1.8s chờ → app chậm.
            // Giờ: chỉ 1 API call (getFilmDetail) → Detail hiển thị nhanh.
            val episodes = res.movie.episodes
                .map { srv ->
                    EpisodeServerDto(
                        serverName = srv.serverName,
                        serverData = srv.items
                            .filter { it.embed.isNotBlank() }
                            .map { ep ->
                                EpisodeDto(
                                    name = ep.name,
                                    slug = ep.slug,
                                    filename = "",
                                    linkEmbed = ep.embed,
                                    linkM3u8 = "",  // ⚡ Sẽ extract lazy khi Play
                                )
                            },
                    )
                }
                .filter { it.serverData.isNotEmpty() }

            AppLogger.success(
                LogTags.REPO,
                "getMovieDetail('$slug') ✓ — ${episodes.size} valid servers, ${episodes.sumOf { it.serverData.size }} total episodes (lazy m3u8)"
            )
            MovieDetailBundle(movie = movie, episodes = episodes)
        } catch (e: Exception) {
            AppLogger.e(LogTags.REPO, "getMovieDetail('$slug') FAILED: ${e.message}", e)
            throw e
        }
    }

    /**
     * ⚡ Extract m3u8 URL cho 1 tập cụ thể (lazy load).
     * Gọi khi user bấm Play — không extract tất cả tập khi mở Detail.
     */
    suspend fun extractM3u8ForEpisode(embedUrl: String): String = io {
        extractM3u8FromEmbed(embedUrl)
    }

    /**
     * Convert embed URL → direct m3u8 URL.
     *
     * Embed URL: https://embedXX.streamc.xyz/embed.php?hash=ABC123
     *  1. Fetch embed page HTML
     *  2. Extract data-obf attribute (base64 JSON)
     *  3. Decode → { sUb: "...", hD: "..." }
     *  4. Direct m3u8 URL = https://embedXX.streamc.xyz/{sUb} (NO ?d=1 — that returns encrypted)
     *
     * Returns empty string if extraction fails (Player will fallback to WebView mode).
     */
    private suspend fun extractM3u8FromEmbed(embedUrl: String): String {
        if (embedUrl.isBlank()) return ""
        return try {
            AppLogger.d(LogTags.REPO, "  → extracting m3u8 from embed: $embedUrl")
            // Use OkHttp to fetch embed page with proper headers
            val request = okhttp3.Request.Builder()
                .url(embedUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36")
                .header("Referer", "https://phim.nguonc.com/")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()
            val response = okHttpClient.newCall(request).execute()
            val html = response.body?.string().orEmpty()
            response.close()
            if (html.isBlank()) {
                AppLogger.w(LogTags.REPO, "  ⚠ embed page empty")
                return ""
            }
            // Extract data-obf="..." attribute
            val obfRegex = Regex("""data-obf="([^"]+)"""")
            val obfMatch = obfRegex.find(html) ?: run {
                AppLogger.w(LogTags.REPO, "  ⚠ data-obf not found in embed page")
                return ""
            }
            val obfEncoded = obfMatch.groupValues[1]
            // Decode base64 → JSON { sUb: "...", hD: "..." }
            val decodedJson = String(java.util.Base64.getDecoder().decode(obfEncoded))
            // Parse JSON manually (avoid kotlinx.serialization for this small case)
            val sUbRegex = Regex(""""sUb"\s*:\s*"([^"]+)"""")
            val sUbMatch = sUbRegex.find(decodedJson)
            if (sUbMatch == null) {
                AppLogger.w(LogTags.REPO, "  ⚠ sUb not found in decoded JSON: $decodedJson")
                return ""
            }
            val sUb = sUbMatch.groupValues[1]
            // Build direct m3u8 URL: same host as embed URL + "/" + sUb (NO ?d=1)
            // embedUrl = https://embed13.streamc.xyz/embed.php?hash=...
            // → m3u8Url = https://embed13.streamc.xyz/{sUb}
            val hostRegex = Regex("""(https?://[^/]+)""")
            val hostMatch = hostRegex.find(embedUrl)
            if (hostMatch == null) {
                AppLogger.w(LogTags.REPO, "  ⚠ cannot extract host from embed URL")
                return ""
            }
            val host = hostMatch.groupValues[1]
            val m3u8Url = "$host/$sUb"
            AppLogger.success(LogTags.REPO, "  ✓ extracted m3u8: $m3u8Url")
            m3u8Url
        } catch (e: Exception) {
            AppLogger.e(LogTags.REPO, "  ⚠ extractM3u8FromEmbed failed: ${e.message}", e)
            ""
        }
    }

    // ============================================================
    // REMOTE — Categories/Countries list (dùng PhimApi)
    // ============================================================

    suspend fun getCategories(): List<CategoryDto> = io {
        api.getCategories().data.items
    }

    suspend fun getCountries(): List<CategoryDto> = io {
        api.getCountries().data.items
    }

    // ---------- Local: Favorites ----------

    fun observeFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.observeAll()

    fun observeIsFavorite(slug: String): Flow<Boolean> = favoriteDao.observeIsFavorite(slug)

    /** @return true nếu sau thao tác phim nằm trong danh sách yêu thích */
    suspend fun toggleFavorite(movie: MovieDetailDto): Boolean = io {
        val isFav = favoriteDao.observeIsFavorite(movie.slug).first()
        if (isFav) {
            favoriteDao.delete(movie.slug)
            false
        } else {
            favoriteDao.upsert(
                FavoriteEntity(
                    slug = movie.slug,
                    name = movie.name,
                    originName = movie.originName,
                    posterUrl = movie.posterUrl,
                    thumbUrl = movie.thumbUrl,
                    year = movie.year,
                    quality = movie.quality,
                    episodeCurrent = movie.episodeCurrent,
                )
            )
            true
        }
    }

    // ---------- Local: History ----------

    fun observeHistory(): Flow<List<HistoryEntity>> = historyDao.observeAll()

    suspend fun getHistory(slug: String): HistoryEntity? = io { historyDao.get(slug) }

    suspend fun saveProgress(
        slug: String,
        name: String,
        posterUrl: String,
        episodeSlug: String,
        episodeName: String,
        positionMs: Long,
    ) = io {
        historyDao.upsert(
            HistoryEntity(
                slug = slug,
                name = name,
                posterUrl = posterUrl,
                episodeSlug = episodeSlug,
                episodeName = episodeName,
                positionMs = positionMs,
            )
        )
    }

    suspend fun removeHistory(slug: String) = io { historyDao.delete(slug) }

    // ============================================================
    // HELPERS — Convert Nguonc DTOs → existing DTOs
    // ============================================================

    /** NguoncMovieItemDto → MovieItemDto (giữ nguyên field names mà UI đang dùng) */
    private fun NguoncMovieItemDto.toMovieItemDto(): MovieItemDto = MovieItemDto(
        id = slug,                          // NguoncApi không có _id, dùng slug
        name = name,
        originName = originName,
        slug = slug,
        posterUrl = posterUrl.absoluteImageUrl(),
        thumbUrl = thumbUrl.absoluteImageUrl(),
        year = 0,                            // NguoncApi không có year ở item, có trong detail
        quality = quality,
        episodeCurrent = currentEpisode,
        lang = lang,
        tmdb = null,                         // NguoncApi không có tmdb
    )

    /** NguoncMovieDetailDto → MovieDetailDto */
    private fun NguoncMovieDetailDto.toMovieDetailDto(): MovieDetailDto {
        // Phân tích category dict: {1: Định dạng, 2: Thể loại, 3: Năm, 4: Quốc gia}
        val categories = category["2"]?.list?.map { CategoryDto(id = it.id, name = it.name, slug = it.id) }
            ?: emptyList()
        val countries = category["4"]?.list?.map { CategoryDto(id = it.id, name = it.name, slug = it.id) }
            ?: emptyList()
        val yearStr = category["3"]?.list?.firstOrNull()?.name ?: "0"
        val year = yearStr.toIntOrNull() ?: 0
        val directors = director?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
            ?: emptyList()
        val actors = casts?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
            ?: emptyList()

        return MovieDetailDto(
            id = id,
            name = name,
            originName = originName,
            slug = slug,
            content = content,
            type = "",  // NguoncApi không trả type
            status = "",
            thumbUrl = thumbUrl.absoluteImageUrl(),
            posterUrl = posterUrl.absoluteImageUrl(),
            trailerUrl = "",
            time = time,
            episodeCurrent = episodeCurrent,
            episodeTotal = episodeTotal,
            quality = quality,
            lang = lang,
            year = year,
            categories = categories,
            countries = countries,
            actors = actors,
            directors = directors,
            tmdb = null,
        )
    }

    /** API đôi khi trả đường dẫn ảnh tương đối — chuẩn hoá về URL tuyệt đối. */
    private fun String.absoluteImageUrl(cdn: String = PhimApi.CDN_IMAGE): String =
        when {
            isBlank() -> this
            startsWith("http://") || startsWith("https://") -> this
            startsWith("/") -> "$cdn$this"
            else -> "$cdn/$this"
        }

    private fun MovieItemDto.normalizeImage(cdn: String = PhimApi.CDN_IMAGE): MovieItemDto =
        copy(posterUrl = posterUrl.absoluteImageUrl(cdn), thumbUrl = thumbUrl.absoluteImageUrl(cdn))

    private fun MovieDetailDto.normalizeImage(cdn: String = PhimApi.CDN_IMAGE): MovieDetailDto =
        copy(posterUrl = posterUrl.absoluteImageUrl(cdn), thumbUrl = thumbUrl.absoluteImageUrl(cdn))

    private suspend fun <T> io(block: suspend () -> T): T =
        withContext(Dispatchers.IO) { block() }
}
