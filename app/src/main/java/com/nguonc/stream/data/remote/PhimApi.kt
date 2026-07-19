package com.nguonc.stream.data.remote

import com.nguonc.stream.data.remote.dto.CategoryListResponse
import com.nguonc.stream.data.remote.dto.MovieDetailResponse
import com.nguonc.stream.data.remote.dto.MovieListResponse
import com.nguonc.stream.data.remote.dto.V1ListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Nguồn C API — https://phimapi.com (backend công khai của phim.nguonc.com).
 * Mọi endpoint đều GET, không cần API key.
 */
interface PhimApi {

    /** type: phim-moi-cap-nhat | phim-le | phim-bo | hoat-hinh | tv-shows */
    @GET("danh-sach/{type}")
    suspend fun getMovieList(
        @Path("type") type: String,
        @Query("page") page: Int = 1,
    ): MovieListResponse

    @GET("phim/{slug}")
    suspend fun getMovieDetail(
        @Path("slug") slug: String,
    ): MovieDetailResponse

    @GET("v1/api/tim-kiem")
    suspend fun search(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = PAGE_SIZE,
    ): V1ListResponse

    @GET("v1/api/the-loai/{slug}")
    suspend fun getByCategory(
        @Path("slug") slug: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = PAGE_SIZE,
    ): V1ListResponse

    @GET("v1/api/quoc-gia/{slug}")
    suspend fun getByCountry(
        @Path("slug") slug: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = PAGE_SIZE,
    ): V1ListResponse

    @GET("the-loai")
    suspend fun getCategories(): CategoryListResponse

    @GET("quoc-gia")
    suspend fun getCountries(): CategoryListResponse

    companion object {
        const val BASE_URL = "https://phimapi.com/"
        const val CDN_IMAGE = "https://phimimg.com"
        const val PAGE_SIZE = 24

        /**
         * User-Agent dùng chung cho MỌI client mạng trong app (OkHttp lẫn ExoPlayer).
         * Một số endpoint /v1/api và CDN segment .m3u8/.ts chặn request thiếu User-Agent
         * hợp lệ — trước đây chỉ OkHttp (API) có header này, ExoPlayer (video) thì không,
         * khiến việc tải danh sách phim thành công nhưng phát video lại bị CDN từ chối.
         */
        const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/126.0 Mobile Safari/537.36"

        /** Loại danh sách hiển thị ở trang chủ / xem thêm */
        const val TYPE_NEW = "phim-moi-cap-nhat"
        const val TYPE_MOVIE = "phim-le"
        const val TYPE_SERIES = "phim-bo"
        const val TYPE_CARTOON = "hoat-hinh"
        const val TYPE_TV_SHOW = "tv-shows"
    }
}
