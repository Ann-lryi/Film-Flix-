package com.nguonc.streamapp.data.api

import com.nguonc.streamapp.data.model.MovieDetailResponse
import com.nguonc.streamapp.data.model.MovieListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NguonCApiService {

    @GET("api/films/phim-moi-cap-nhat")
    suspend fun getRecentlyUpdated(
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>

    @GET("api/film/{slug}")
    suspend fun getMovieDetail(
        @Path("slug") slug: String
    ): Response<MovieDetailResponse>

    @GET("api/films/search")
    suspend fun searchMovies(
        @Query("keyword") keyword: String
    ): Response<MovieListResponse>

    @GET("api/films/the-loai/{category_slug}")
    suspend fun getMoviesByCategory(
        @Path("category_slug") categorySlug: String,
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>

    @GET("api/films/quoc-gia/{country_slug}")
    suspend fun getMoviesByCountry(
        @Path("country_slug") countrySlug: String,
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>
}
