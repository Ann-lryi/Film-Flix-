package com.nguoncflix.api

import com.nguoncflix.data.models.ApiResponse
import com.nguoncflix.data.models.Movie
import com.nguoncflix.data.models.MovieDetail
import com.nguoncflix.data.models.EpisodeServer
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("danh-sach/phim-moi-cap-nhat")
    suspend fun getNewMovies(
        @Query("page") page: Int = 1
    ): ApiResponse<Movie>

    @GET("v1/api/danh-sach/{type}")
    suspend fun getMoviesByType(
        @Path("type") type: String, // phim-bo, phim-le, hoat-hinh, tv-shows
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<Movie>

    @GET("phim/{slug}")
    suspend fun getMovieDetail(
        @Path("slug") slug: String
    ): ApiResponse<MovieDetail>

    // Search support
    @GET("v1/api/tim-kiem")
    suspend fun searchMovies(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<Movie>
}
