package com.nguonc.stream.data.remote

import com.nguonc.stream.data.remote.dto.NguoncDetailResponse
import com.nguonc.stream.data.remote.dto.NguoncListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * NguoncApi — API gốc của phim.nguonc.com
 *
 * Khác với PhimApi (phimapi.com — public mirror), NguoncApi gọi thẳng
 * backend chính thức của trang phim.nguonc.com. Cấu trúc response khác
 * phimapi.com (xem NguoncListResponse / NguoncDetailResponse).
 *
 * Endpoint mapping:
 *  - Phim mới cập nhật: /films/phim-moi-cap-nhat?page=N
 *  - Phim lẻ:           /films/danh-sach/phim-le?page=N
 *  - Phim bộ:           /films/danh-sach/phim-bo?page=N
 *  - Hoạt hình:         /films/the-loai/hoat-hinh?page=N (dùng endpoint the-loai)
 *  - Chi tiết:          /film/{slug}  (note: "film" singular)
 *  - Tìm kiếm:          /films/search?keyword=...&page=N
 */
interface NguoncApi {

    /** Phim mới cập nhật */
    @GET("films/phim-moi-cap-nhat")
    suspend fun getPhimMoiCapNhat(
        @Query("page") page: Int = 1,
    ): NguoncListResponse

    /** Phim lẻ / Phim bộ — dùng /films/danh-sach/{type} */
    @GET("films/danh-sach/{type}")
    suspend fun getDanhSach(
        @Path("type") type: String,
        @Query("page") page: Int = 1,
    ): NguoncListResponse

    /** Theo thể loại (vd: hoat-hinh, hanh-dong, ...) — dùng /films/the-loai/{slug} */
    @GET("films/the-loai/{slug}")
    suspend fun getTheLoai(
        @Path("slug") slug: String,
        @Query("page") page: Int = 1,
    ): NguoncListResponse

    /** Chi tiết phim — dùng /film/{slug} (note: "film" singular) */
    @GET("film/{slug}")
    suspend fun getFilmDetail(
        @Path("slug") slug: String,
    ): NguoncDetailResponse

    /** Tìm kiếm */
    @GET("films/search")
    suspend fun search(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
    ): NguoncListResponse

    companion object {
        const val BASE_URL = "https://phim.nguonc.com/api/"
    }
}
