package com.aho.yunphim.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model ánh xạ theo đúng JSON schema thật của phim.nguonc.com - ĐÃ XÁC MINH qua source code
 * plugin CloudStream "NguonC" đang chạy thật của Aho (không còn là suy đoán theo họ ophim/kkphim
 * như bản đầu). Field then chốt: "description" (không phải "content"), "language" (không phải
 * "lang"), "embed"/"m3u8" (không có tiền tố "link_"), "items"/"list" cho danh sách tập trong 1
 * server (API hedge cả 2 tên, đọc cả hai).
 *
 * Vẫn giữ mọi field không cốt lõi ở dạng nullable - JSON thiếu/thừa field vẫn không crash app,
 * MovieRepository bọc try/catch quanh toàn bộ lệnh gọi mạng để lộ lỗi rõ ràng thay vì crash nếu
 * site đổi schema trong tương lai.
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
    val language: String? = null,
    @SerialName("current_episode") val currentEpisode: String? = null,
) {
    /** Ưu tiên poster (dọc) cho lưới, fallback thumb nếu poster rỗng. */
    val displayImage: String? get() = posterUrl?.takeIf { it.isNotBlank() } ?: thumbUrl
}

@Serializable
data class MovieDetailResponse(
    val status: String? = null,
    val movie: MovieDetail? = null,
)

@Serializable
data class MovieDetail(
    val id: String? = null,
    val name: String? = null,
    val slug: String? = null,
    @SerialName("origin_name") val originName: String? = null,
    val description: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    val quality: String? = null,
    val language: String? = null,
    val time: String? = null,
    val director: String? = null,
    val casts: String? = null,
    @SerialName("episodes") val servers: List<ServerGroup> = emptyList(),
) {
    val displayBackdrop: String? get() = posterUrl?.takeIf { it.isNotBlank() } ?: thumbUrl
    val displayPoster: String? get() = thumbUrl?.takeIf { it.isNotBlank() } ?: posterUrl
}

@Serializable
data class ServerGroup(
    @SerialName("server_name") val serverName: String? = null,
    val name: String? = null,
    // API thật hedge cả 2 tên field cho danh sách tập trong 1 server (xác nhận từ plugin
    // CloudStream đang chạy thật - bản thân tác giả plugin cũng không chắc field nào sẽ tới,
    // nên đọc cả hai và ưu tiên "items").
    val items: List<EpisodeItem>? = null,
    val list: List<EpisodeItem>? = null,
) {
    val episodes: List<EpisodeItem> get() = items ?: list ?: emptyList()
    val displayName: String? get() = serverName?.takeIf { it.isNotBlank() } ?: name
}

@Serializable
data class EpisodeItem(
    val name: String? = null,
    // KHÔNG phải "link_embed"/"link_m3u8" như giả định ban đầu - field thật là "embed"/"m3u8"
    // (xác nhận từ plugin CloudStream NguonC đang chạy thật của Aho, không phải suy đoán).
    val embed: String? = null,
    val m3u8: String? = null,
) {
    val displayName: String get() = name?.takeIf { it.isNotBlank() } ?: "Tập"
}
