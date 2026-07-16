package com.aho.yunphim.data.remote

import com.aho.yunphim.data.model.MovieDetailResponse
import com.aho.yunphim.data.model.MovieListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * ⚠️ KHÔNG ĐỦ DỮ LIỆU ĐỂ XÁC MINH 100%: path chính xác của API JSON trên phim.nguonc.com.
 *
 * Domain bị Cloudflare chặn request tự động (đã thử fetch trực tiếp trang chủ, trang phim, các
 * forum thảo luận - đều bị bot detection chặn; GitHub code search cũng không tra ra provider
 * CloudStream cũ tham chiếu path cụ thể). Path bên dưới là GIẢ THUYẾT tốt nhất, dựa trên:
 *  1. Trang /phim/{slug} của chính nguonc.com có tab "API" tách riêng khỏi tab "Nội dung phim"
 *     (xác nhận qua kết quả tìm kiếm) -> API nằm ở URL khác trang web, không content-negotiate
 *     trên cùng 1 URL.
 *  2. Quy ước /api/film/{slug} + /api/films/danh-sach/{type} là quy ước phổ biến của họ API
 *     "ophim-style" mà nguonc.com được nhiều nguồn liệt kê chung nhóm (Ophim, KKPhim, NguonC).
 *
 * CÁCH XÁC MINH: mở 1 URL .../phim/{slug} bất kỳ (vd. https://phim.nguonc.com/phim/gia-nghiep)
 * trên trình duyệt thật hoặc qua StreamBrowser, bấm tab "API", đọc URL JSON thật hiện ra.
 * Nếu khác bên dưới -> chỉ sửa object này, không đụng chỗ nào khác trong app.
 */
object NguonCEndpoints {
    const val BASE_URL = "https://phim.nguonc.com/"
    const val LIST_PATH = "api/films/danh-sach/{type}"
    const val DETAIL_PATH = "api/film/{slug}"
    const val SEARCH_PATH = "api/films/search"

    /** Type đã xác nhận tồn tại ở dạng trang web /danh-sach/{type} trên chính nguonc.com. */
    object ListType {
        const val NEW_UPDATE = "phim-moi-cap-nhat"
        const val SINGLE = "phim-le"
        const val SERIES = "phim-bo"
        const val ANIME = "hoat-hinh"
        const val TV_SHOW = "tv-shows"
    }
}

interface NguonCApi {

    @GET(NguonCEndpoints.LIST_PATH)
    suspend fun getMovieList(
        @Path("type") type: String,
        @Query("page") page: Int,
    ): MovieListResponse

    @GET(NguonCEndpoints.DETAIL_PATH)
    suspend fun getMovieDetail(
        @Path("slug") slug: String,
    ): MovieDetailResponse

    @GET(NguonCEndpoints.SEARCH_PATH)
    suspend fun search(
        @Query("keyword") keyword: String,
        @Query("page") page: Int,
    ): MovieListResponse
}
