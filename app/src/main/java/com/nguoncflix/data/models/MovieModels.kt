package com.nguoncflix.data.models

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper for phimapi.com.
 *
 * The endpoint shape varies:
 *  - /danh-sach/phim-moi-cap-nhat → uses `items` (List<Movie>)
 *  - /v1/api/danh-sach/{type}    → uses `data.items` (List<Movie>)
 *  - /v1/api/tim-kiem            → uses `data.items` (List<Movie>)
 *  - /phim/{slug}                → uses `movie` + `episodes`
 *
 * We accept all shapes as nullable fields so a single model can deserialize
 * any endpoint response.
 */
data class ApiResponse<T>(
    val status: Boolean? = null,
    val msg: String? = null,
    val items: List<T>? = null,
    val data: DataWrapper<T>? = null,
    val movie: MovieDetail? = null,
    val episodes: List<EpisodeServer>? = null
) {
    /**
     * Best-effort list of items regardless of which wrapper the endpoint used.
     */
    fun resolveItems(): List<T>? = items ?: data?.items
}

data class DataWrapper<T>(
    val items: List<T> = emptyList()
)

data class Movie(
    @SerializedName("_id") val id: String,
    val name: String,
    val slug: String,
    @SerializedName("origin_name") val originName: String? = null,
    @SerializedName("poster_url") val posterUrl: String = "",
    @SerializedName("thumb_url") val thumbUrl: String = "",
    val year: Int? = null,
    @SerializedName("episode_current") val episodeCurrent: String? = null,
    val quality: String? = null,
    val lang: String? = null,
    val type: String? = null,
    @SerializedName("category") val category: List<Category>? = null,
    @SerializedName("country") val country: List<Country>? = null
)

data class Category(
    val id: String? = null,
    val name: String,
    val slug: String
)

data class Country(
    val id: String? = null,
    val name: String,
    val slug: String
)

data class MovieDetail(
    @SerializedName("_id") val id: String,
    val name: String,
    val slug: String,
    @SerializedName("origin_name") val originName: String? = null,
    @SerializedName("poster_url") val posterUrl: String = "",
    @SerializedName("thumb_url") val thumbUrl: String = "",
    val year: Int? = null,
    val content: String? = null,
    @SerializedName("episode_current") val episodeCurrent: String? = null,
    @SerializedName("episode_total") val episodeTotal: Int? = null,
    val quality: String? = null,
    val lang: String? = null,
    val type: String? = null, // "single" or "series"
    val actor: List<String>? = null,
    val director: List<String>? = null,
    @SerializedName("category") val categories: List<Category>? = null,
    @SerializedName("country") val countries: List<Country>? = null,
    @SerializedName("trailer_url") val trailerUrl: String? = null
)

data class EpisodeServer(
    @SerializedName("server_name") val serverName: String,
    @SerializedName("server_data") val serverData: List<EpisodeData> = emptyList()
)

data class EpisodeData(
    val name: String,
    val slug: String,
    @SerializedName("link_m3u8") val linkM3u8: String? = null,
    @SerializedName("link_embed") val linkEmbed: String? = null
)
