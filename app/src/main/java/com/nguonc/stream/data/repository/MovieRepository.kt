package com.nguonc.stream.data.repository

import com.nguonc.stream.data.local.FavoriteDao
import com.nguonc.stream.data.local.FavoriteEntity
import com.nguonc.stream.data.local.HistoryDao
import com.nguonc.stream.data.local.HistoryEntity
import com.nguonc.stream.data.local.RecentSearchDao
import com.nguonc.stream.data.local.RecentSearchEntity
import com.nguonc.stream.data.remote.PhimApi
import com.nguonc.stream.data.remote.dto.CategoryDto
import com.nguonc.stream.data.remote.dto.EpisodeServerDto
import com.nguonc.stream.data.remote.dto.MovieDetailDto
import com.nguonc.stream.data.remote.dto.MovieItemDto
import com.nguonc.stream.data.remote.dto.PaginationDto
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
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao,
    private val recentSearchDao: RecentSearchDao,
) {
    // ---------- Remote ----------

    suspend fun getMovieList(type: String, page: Int): MoviePage = io {
        val res = api.getMovieList(type, page)
        MoviePage(res.items.map { it.normalizeImage() }, res.pagination)
    }

    suspend fun search(keyword: String, page: Int): MoviePage = io {
        val res = api.search(keyword = keyword, page = page)
        val cdn = res.data.cdnImage
        MoviePage(
            items = res.data.items.map { it.normalizeImage(cdn) },
            pagination = res.data.params.pagination,
        )
    }

    suspend fun getByCategory(slug: String, page: Int): MoviePage = io {
        val res = api.getByCategory(slug, page)
        MoviePage(
            items = res.data.items.map { it.normalizeImage(res.data.cdnImage) },
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

    suspend fun getMovieDetail(slug: String): MovieDetailBundle = io {
        val res = api.getMovieDetail(slug)
        MovieDetailBundle(
            movie = res.movie.normalizeImage(),
            episodes = res.episodes.filter { it.serverData.isNotEmpty() },
        )
    }

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
        durationMs: Long = 0L,
        serverIndex: Int = 0,
    ) = io {
        historyDao.upsert(
            HistoryEntity(
                slug = slug,
                name = name,
                posterUrl = posterUrl,
                episodeSlug = episodeSlug,
                episodeName = episodeName,
                positionMs = positionMs,
                durationMs = durationMs,
                serverIndex = serverIndex,
            )
        )
    }

    suspend fun removeHistory(slug: String) = io { historyDao.delete(slug) }

    // ---------- Local: Recent searches ----------

    fun observeRecentSearches(): Flow<List<RecentSearchEntity>> = recentSearchDao.observeRecent()

    suspend fun addRecentSearch(query: String) = io {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) recentSearchDao.upsert(RecentSearchEntity(query = trimmed))
    }

    suspend fun removeRecentSearch(query: String) = io { recentSearchDao.delete(query) }

    suspend fun clearRecentSearches() = io { recentSearchDao.clearAll() }

    // ---------- Helpers ----------

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
