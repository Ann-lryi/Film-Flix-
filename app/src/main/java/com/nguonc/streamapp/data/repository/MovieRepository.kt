package com.nguonc.streamapp.data.repository

import com.nguonc.streamapp.data.api.RetrofitClient
import com.nguonc.streamapp.data.local.FavoriteDao
import com.nguonc.streamapp.data.local.FavoriteEntity
import com.nguonc.streamapp.data.model.MovieDetail
import com.nguonc.streamapp.data.model.MovieDetailResponse
import com.nguonc.streamapp.data.model.MovieItem
import com.nguonc.streamapp.data.model.MovieListResponse
import com.nguonc.streamapp.data.model.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

class MovieRepository(
    private val favoriteDao: FavoriteDao
) {
    private val api = RetrofitClient.apiService

    fun getRecentlyUpdated(page: Int = 1): Flow<NetworkResult<MovieListResponse>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = api.getRecentlyUpdated(page)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(handleError(response)))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Lỗi kết nối hoặc tường lửa Cloudflare đang bảo vệ máy chủ phim.nguonc.com. Vui lòng thử lại sau (${e.localizedMessage})"))
        }
    }.flowOn(Dispatchers.IO)

    fun getMovieDetail(slug: String): Flow<NetworkResult<MovieDetailResponse>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = api.getMovieDetail(slug)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(handleError(response)))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Lỗi tải chi tiết phim từ phim.nguonc.com: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    fun searchMovies(keyword: String): Flow<NetworkResult<MovieListResponse>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = api.searchMovies(keyword)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(handleError(response)))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Lỗi tìm kiếm trên phim.nguonc.com: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    fun getMoviesByCategory(categorySlug: String, page: Int = 1): Flow<NetworkResult<MovieListResponse>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = if (categorySlug == "phim-moi-cap-nhat") {
                api.getRecentlyUpdated(page)
            } else {
                api.getMoviesByCategory(categorySlug, page)
            }
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(handleError(response)))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Lỗi tải danh mục từ phim.nguonc.com: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    fun getMoviesByCountry(countrySlug: String, page: Int = 1): Flow<NetworkResult<MovieListResponse>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = api.getMoviesByCountry(countrySlug, page)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(handleError(response)))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Lỗi tải quốc gia từ phim.nguonc.com: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    private fun handleError(response: Response<*>): String {
        return when (response.code()) {
            403 -> "Máy chủ phim.nguonc.com tạm thời chặn yêu cầu (HTTP 403 / Cloudflare Challenge). Vui lòng thử lại."
            404 -> "Không tìm thấy phim trên máy chủ (HTTP 404)."
            500, 502, 503, 504 -> "Máy chủ phim.nguonc.com đang bảo trì hoặc quá tải (HTTP ${response.code()})."
            else -> "Lỗi phản hồi từ máy chủ: HTTP ${response.code()}"
        }
    }

    // Local Storage Operations
    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    fun isFavorite(slug: String): Flow<Boolean> = favoriteDao.isFavorite(slug)

    suspend fun toggleFavorite(item: MovieItem, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            favoriteDao.deleteFavoriteBySlug(item.slug ?: return)
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    slug = item.slug ?: return,
                    name = item.name ?: "Không tên",
                    originName = item.originName ?: "",
                    thumbUrl = item.getFullThumbUrl(),
                    posterUrl = item.getFullPosterUrl()
                )
            )
        }
    }

    suspend fun toggleFavoriteDetail(movie: MovieDetail, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            favoriteDao.deleteFavoriteBySlug(movie.slug ?: return)
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    slug = movie.slug ?: return,
                    name = movie.name ?: "Không tên",
                    originName = movie.originName ?: "",
                    thumbUrl = movie.getFullThumbUrl(),
                    posterUrl = movie.getFullPosterUrl()
                )
            )
        }
    }
}
