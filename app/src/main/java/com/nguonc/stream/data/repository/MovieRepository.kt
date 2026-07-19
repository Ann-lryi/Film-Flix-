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
        val res = when (type) {
            "phim-moi-cap-nhat" -> nguoncApi.getPhimMoiCapNhat(page)
            "phim-le", "phim-bo", "tv-shows" -> nguoncApi.getDanhSach(type, page)
            "hoat-hinh" -> nguoncApi.getTheLoai("hoat-hinh", page)
            else -> nguoncApi.getDanhSach(type, page)
        }
        MoviePage(
            items = res.items.map { it.toMovieItemDto() },
            pagination = PaginationDto(
                totalItems = res.paginate.totalItems,
                totalItemsPerPage = res.paginate.itemsPerPage,
                currentPage = res.paginate.currentPage,
                totalPages = res.paginate.totalPage,
            ),
        )
    }

    /** Tìm kiếm — dùng NguoncApi */
    suspend fun search(keyword: String, page: Int): MoviePage = io {
        val res = nguoncApi.search(keyword, page)
        MoviePage(
            items = res.items.map { it.toMovieItemDto() },
            pagination = PaginationDto(
                totalItems = res.paginate.totalItems,
                totalItemsPerPage = res.paginate.itemsPerPage,
                currentPage = res.paginate.currentPage,
                totalPages = res.paginate.totalPage,
            ),
        )
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
        val res = nguoncApi.getFilmDetail(slug)
        val movie = res.movie.toMovieDetailDto()
        // Chỉ giữ server có ít nhất 1 tập có embed URL hợp lệ.
        // Detail và Player dùng cùng danh sách, tránh lệch index.
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
                                // NguoncApi chỉ trả embed URL (iframe player),
                                // không trả link_m3u8 trực tiếp. ExoPlayer không
                                // play được embed URL → Player sẽ dùng WebView.
                                linkM3u8 = "",
                            )
                        },
                )
            }
            .filter { it.serverData.isNotEmpty() }
        MovieDetailBundle(movie = movie, episodes = episodes)
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
        posterUrl = posterUrl.normalizeImageUrl(),
        thumbUrl = thumbUrl.normalizeImageUrl(),
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
            thumbUrl = thumbUrl.normalizeImageUrl(),
            posterUrl = posterUrl.normalizeImageUrl(),
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
