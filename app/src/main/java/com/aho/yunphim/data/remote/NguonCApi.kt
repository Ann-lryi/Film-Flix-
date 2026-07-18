package com.aho.yunphim.data.remote

import com.aho.yunphim.data.model.MovieDetailResponse
import com.aho.yunphim.data.model.MovieListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Path ĐÃ XÁC MINH qua source code plugin CloudStream "NguonC" đang chạy thật của Aho (không
 * còn là giả thuyết như bản đầu). Mỗi tab có path riêng, KHÔNG dùng chung 1 template - đây là
 * lý do NguonCApi dùng @Url động thay vì {type} path param.
 */
object NguonCEndpoints {
    const val BASE_URL = "https://phim.nguonc.com/"

    object ListPath {
        const val NEW_UPDATE = "api/films/phim-moi-cap-nhat"
        const val SINGLE = "api/films/danh-sach/phim-le"
        const val SERIES = "api/films/danh-sach/phim-bo"
        const val ANIME = "api/films/the-loai/hoat-hinh"
        const val ADULT_18 = "api/films/the-loai/phim-18"
    }
}

interface NguonCApi {

    @GET
    suspend fun getMovieList(@Url url: String, @Query("page") page: Int): MovieListResponse

    @GET("api/film/{slug}")
    suspend fun getMovieDetail(@Path("slug") slug: String): MovieDetailResponse

    @GET("api/films/search")
    suspend fun search(@Query("keyword") keyword: String): MovieListResponse
}
