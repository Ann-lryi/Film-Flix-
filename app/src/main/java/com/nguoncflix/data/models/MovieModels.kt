package com.nguoncflix.data.models

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val status: Boolean,
    val msg: String?,
    val items: List<T>? = null,
    val data: DataWrapper<T>? = null,
    val movie: MovieDetail? = null,
    val episodes: List<EpisodeServer>? = null
)

data class DataWrapper<T>(
    val items: List<T>
)

data class Movie(
    @SerializedName("_id") val id: String,
    val name: String,
    val slug: String,
    @SerializedName("origin_name") val originName: String?,
    @SerializedName("poster_url") val posterUrl: String,
    @SerializedName("thumb_url") val thumbUrl: String,
    val year: Int?,
    @SerializedName("episode_current") val episodeCurrent: String?,
    val quality: String?,
    val lang: String?,
    @SerializedName("type") val type: String? = null,
    @SerializedName("category") val category: List<Category>? = null,
    @SerializedName("country") val country: List<Country>? = null
)

data class Category(
    val name: String,
    val slug: String
)

data class Country(
    val name: String,
    val slug: String
)

data class MovieDetail(
    @SerializedName("_id") val id: String,
    val name: String,
    val slug: String,
    @SerializedName("origin_name") val originName: String?,
    @SerializedName("poster_url") val posterUrl: String,
    @SerializedName("thumb_url") val thumbUrl: String,
    val year: Int?,
    val content: String?,
    @SerializedName("episode_current") val episodeCurrent: String?,
    @SerializedName("episode_total") val episodeTotal: Int?,
    val quality: String?,
    val lang: String?,
    val type: String?, // "single" or "series"
    val actor: List<String>?,
    val director: List<String>?,
    @SerializedName("category") val categories: List<Category>?,
    @SerializedName("country") val countries: List<Country>?,
    @SerializedName("trailer_url") val trailerUrl: String?
)

data class EpisodeServer(
    @SerializedName("server_name") val serverName: String,
    @SerializedName("server_data") val serverData: List<EpisodeData>
)

data class EpisodeData(
    val name: String,
    val slug: String,
    @SerializedName("link_m3u8") val linkM3u8: String?,
    @SerializedName("link_embed") val linkEmbed: String?
)

data class MovieListResponse(
    val status: Boolean,
    val items: List<Movie>
)
