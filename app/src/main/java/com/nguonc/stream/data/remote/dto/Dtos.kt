package com.nguonc.stream.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO khớp 1:1 với JSON của Nguồn C API (phimapi.com).
 * Lưu ý: `status` của API không nhất quán (boolean ở /danh-sach, string ở /v1/api)
 * nên ta bỏ qua hoàn toàn, chỉ parse payload cần dùng.
 */

// ---------- Chung ----------

@Serializable
data class PaginationDto(
    @SerialName("totalItems") val totalItems: Int = 0,
    @SerialName("totalItemsPerPage") val totalItemsPerPage: Int = 0,
    @SerialName("currentPage") val currentPage: Int = 1,
    @SerialName("totalPages") val totalPages: Int = 1,
)

@Serializable
data class TmdbDto(
    @SerialName("type") val type: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
)

@Serializable
data class CategoryDto(
    @SerialName("_id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("slug") val slug: String = "",
)

// ---------- Item phim trong danh sách ----------

@Serializable
data class MovieItemDto(
    @SerialName("_id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("origin_name") val originName: String = "",
    @SerialName("slug") val slug: String = "",
    @SerialName("poster_url") val posterUrl: String = "",
    @SerialName("thumb_url") val thumbUrl: String = "",
    @SerialName("year") val year: Int = 0,
    @SerialName("quality") val quality: String? = null,
    @SerialName("episode_current") val episodeCurrent: String? = null,
    @SerialName("lang") val lang: String? = null,
    @SerialName("tmdb") val tmdb: TmdbDto? = null,
)

// ---------- /danh-sach/{type} ----------

@Serializable
data class MovieListResponse(
    @SerialName("items") val items: List<MovieItemDto> = emptyList(),
    @SerialName("pagination") val pagination: PaginationDto = PaginationDto(),
)

// ---------- /v1/api/* (tim-kiem, the-loai/{slug}, quoc-gia/{slug}) ----------

@Serializable
data class V1Params(
    @SerialName("pagination") val pagination: PaginationDto = PaginationDto(),
)

@Serializable
data class V1Data(
    @SerialName("titlePage") val titlePage: String = "",
    @SerialName("items") val items: List<MovieItemDto> = emptyList(),
    @SerialName("params") val params: V1Params = V1Params(),
    @SerialName("APP_DOMAIN_CDN_IMAGE") val cdnImage: String = "https://phimimg.com",
)

@Serializable
data class V1ListResponse(
    @SerialName("data") val data: V1Data = V1Data(),
)

// ---------- /the-loai & /quoc-gia ----------

@Serializable
data class CategoryListData(
    @SerialName("items") val items: List<CategoryDto> = emptyList(),
)

@Serializable
data class CategoryListResponse(
    @SerialName("data") val data: CategoryListData = CategoryListData(),
)

// ---------- /phim/{slug} ----------

@Serializable
data class EpisodeDto(
    @SerialName("name") val name: String = "",
    @SerialName("slug") val slug: String = "",
    @SerialName("filename") val filename: String = "",
    @SerialName("link_embed") val linkEmbed: String = "",
    @SerialName("link_m3u8") val linkM3u8: String = "",
)

@Serializable
data class EpisodeServerDto(
    @SerialName("server_name") val serverName: String = "",
    @SerialName("server_data") val serverData: List<EpisodeDto> = emptyList(),
)

@Serializable
data class MovieDetailDto(
    @SerialName("_id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("origin_name") val originName: String = "",
    @SerialName("slug") val slug: String = "",
    @SerialName("content") val content: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("status") val status: String = "",
    @SerialName("thumb_url") val thumbUrl: String = "",
    @SerialName("poster_url") val posterUrl: String = "",
    @SerialName("trailer_url") val trailerUrl: String = "",
    @SerialName("time") val time: String = "",
    @SerialName("episode_current") val episodeCurrent: String = "",
    @SerialName("episode_total") val episodeTotal: String = "",
    @SerialName("quality") val quality: String = "",
    @SerialName("lang") val lang: String = "",
    @SerialName("year") val year: Int = 0,
    @SerialName("category") val categories: List<CategoryDto> = emptyList(),
    @SerialName("country") val countries: List<CategoryDto> = emptyList(),
    @SerialName("actor") val actors: List<String> = emptyList(),
    @SerialName("director") val directors: List<String> = emptyList(),
    @SerialName("tmdb") val tmdb: TmdbDto? = null,
)

@Serializable
data class MovieDetailResponse(
    @SerialName("movie") val movie: MovieDetailDto = MovieDetailDto(),
    @SerialName("episodes") val episodes: List<EpisodeServerDto> = emptyList(),
)
