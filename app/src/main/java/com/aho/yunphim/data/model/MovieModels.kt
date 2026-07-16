package com.aho.yunphim.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model ánh xạ theo quy ước chung của họ API "ophim/nguonc" (status/items/movie/episodes),
 * xác nhận qua tài liệu ophim1.com + phimapi.com (cùng hệ) và bên tổng hợp phimtop1.asia
 * (tổng hợp trực tiếp từ Ophim + KKPhim + NguonC).
 *
 * KHÔNG ĐỦ DỮ LIỆU ĐỂ XÁC MINH 100% field lẻ đúng với nguonc.com thực tế (site chặn bot,
 * không fetch trực tiếp được response mẫu). Vì vậy mọi field không cốt lõi đều nullable với
 * default null/rỗng - JSON thiếu field hoặc thừa field lạ sẽ không làm crash app, chỉ hiện
 * rỗng/ẩn phần UI tương ứng. NguonCRepository bọc try/catch quanh toàn bộ lệnh gọi mạng để
 * nếu shape lệch nặng hơn dự kiến, người dùng thấy màn hình lỗi rõ ràng thay vì crash.
 */

@Serializable
data class MovieListResponse(
    val status: String? = null,
    val items: List<MovieSummary> = emptyList(),
    val paginate: Pagination? = null,
    val pagination: Pagination? = null,
) {
    val effectivePagination: Pagination? get() = pagination ?: paginate
}

@Serializable
data class Pagination(
    @SerialName("current_page") val currentPageSnake: Int? = null,
    val currentPage: Int? = null,
    @SerialName("total_page") val totalPageSnake: Int? = null,
    val totalPages: Int? = null,
) {
    val page: Int get() = currentPage ?: currentPageSnake ?: 1
    val lastPage: Int get() = totalPages ?: totalPageSnake ?: page
}

@Serializable
data class MovieSummary(
    val id: String? = null,
    val name: String? = null,
    val slug: String? = null,
    @SerialName("origin_name") val originName: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    val quality: String? = null,
    val lang: String? = null,
) {
    /** Ưu tiên poster (dọc) cho lưới, fallback thumb nếu poster rỗng. */
    val displayImage: String? get() = posterUrl?.takeIf { it.isNotBlank() } ?: thumbUrl
}

@Serializable
data class MovieDetailResponse(
    val status: String? = null,
    val movie: MovieDetail? = null,
    val episodes: List<ServerGroup> = emptyList(),
)

@Serializable
data class MovieDetail(
    val id: String? = null,
    val name: String? = null,
    val slug: String? = null,
    @SerialName("origin_name") val originName: String? = null,
    val content: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    val quality: String? = null,
    val lang: String? = null,
    val time: String? = null,
    val status: String? = null,
) {
    val displayBackdrop: String? get() = posterUrl?.takeIf { it.isNotBlank() } ?: thumbUrl
    val displayPoster: String? get() = thumbUrl?.takeIf { it.isNotBlank() } ?: posterUrl
}

@Serializable
data class ServerGroup(
    @SerialName("server_name") val serverName: String? = null,
    @SerialName("server_data") val serverData: List<EpisodeItem> = emptyList(),
)

@Serializable
data class EpisodeItem(
    val name: String? = null,
    val slug: String? = null,
    @SerialName("link_embed") val linkEmbed: String? = null,
    @SerialName("link_m3u8") val linkM3u8: String? = null,
) {
    val displayName: String get() = name?.takeIf { it.isNotBlank() } ?: slug ?: "Tập"
}
