package com.nguonc.streamapp.data.model

import com.google.gson.annotations.SerializedName

data class MovieListResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("items") val items: List<MovieItem>? = null,
    @SerializedName("pagination") val pagination: Pagination? = null
)

data class Pagination(
    @SerializedName("totalItems") val totalItems: Int? = 0,
    @SerializedName("totalItemsPerPage") val totalItemsPerPage: Int? = 0,
    @SerializedName("currentPage") val currentPage: Int? = 1,
    @SerializedName("totalPages") val totalPages: Int? = 1
)

data class MovieItem(
    @SerializedName("name") val name: String? = "",
    @SerializedName("slug") val slug: String? = "",
    @SerializedName("origin_name") val originName: String? = "",
    @SerializedName("thumb_url") val thumbUrl: String? = "",
    @SerializedName("poster_url") val posterUrl: String? = "",
    @SerializedName("year") val year: Int? = 0
) {
    fun getFullThumbUrl(): String {
        val url = thumbUrl ?: ""
        return if (url.startsWith("http")) url else "https://phimimg.com/$url"
    }

    fun getFullPosterUrl(): String {
        val url = posterUrl ?: thumbUrl ?: ""
        return if (url.startsWith("http")) url else "https://phimimg.com/$url"
    }
}

data class MovieDetailResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("movie") val movie: MovieDetail? = null,
    @SerializedName("episodes") val episodes: List<EpisodeServer>? = null
)

data class MovieDetail(
    @SerializedName("name") val name: String? = "",
    @SerializedName("slug") val slug: String? = "",
    @SerializedName("origin_name") val originName: String? = "",
    @SerializedName("content") val content: String? = "",
    @SerializedName("thumb_url") val thumbUrl: String? = "",
    @SerializedName("poster_url") val posterUrl: String? = "",
    @SerializedName("year") val year: Int? = 0,
    @SerializedName("quality") val quality: String? = "",
    @SerializedName("time") val time: String? = "",
    @SerializedName("episode_current") val episodeCurrent: String? = "",
    @SerializedName("category") val categoryList: List<CategoryItem>? = null,
    @SerializedName("country") val countryList: List<CountryItem>? = null
) {
    fun getFullThumbUrl(): String {
        val url = thumbUrl ?: ""
        return if (url.startsWith("http")) url else "https://phimimg.com/$url"
    }

    fun getFullPosterUrl(): String {
        val url = posterUrl ?: thumbUrl ?: ""
        return if (url.startsWith("http")) url else "https://phimimg.com/$url"
    }
}

data class CategoryItem(
    @SerializedName("id") val id: String? = "",
    @SerializedName("name") val name: String? = "",
    @SerializedName("slug") val slug: String? = ""
)

data class CountryItem(
    @SerializedName("id") val id: String? = "",
    @SerializedName("name") val name: String? = "",
    @SerializedName("slug") val slug: String? = ""
)

data class EpisodeServer(
    @SerializedName("server_name") val serverName: String? = "",
    @SerializedName("items") val items: List<EpisodeItem>? = null,
    @SerializedName("server_data") val serverData: List<EpisodeItem>? = null
) {
    fun getEpisodeList(): List<EpisodeItem> {
        return items ?: serverData ?: emptyList()
    }
}

data class EpisodeItem(
    @SerializedName("name") val name: String? = "",
    @SerializedName("slug") val slug: String? = "",
    @SerializedName("embed") val embedUrl: String? = "",
    @SerializedName("link_embed") val linkEmbedUrl: String? = "",
    @SerializedName("m3u8") val m3u8Url: String? = "",
    @SerializedName("link_m3u8") val linkM3u8Url: String? = ""
) {
    fun getBestStreamUrl(): String {
        val m3u8 = m3u8Url?.takeIf { it.isNotEmpty() } ?: linkM3u8Url ?: ""
        val embed = embedUrl?.takeIf { it.isNotEmpty() } ?: linkEmbedUrl ?: ""
        return if (m3u8.isNotEmpty()) m3u8 else embed
    }
}

data class FilterCategory(
    val name: String,
    val slug: String
)

object Categories {
    val list = listOf(
        FilterCategory("Mới nhất", "phim-moi-cap-nhat"),
        FilterCategory("Hành Động", "hanh-dong"),
        FilterCategory("Tình Cảm", "tinh-cam"),
        FilterCategory("Hoạt Hình", "hoat-hinh"),
        FilterCategory("Cổ Trang", "co-trang"),
        FilterCategory("Hài Hước", "hai-huoc"),
        FilterCategory("Kinh Dị", "kinh-di"),
        FilterCategory("Tâm Lý", "tam-ly"),
        FilterCategory("Khoa Học", "khoa-hoc")
    )
}

object Countries {
    val list = listOf(
        FilterCategory("Trung Quốc", "trung-quoc"),
        FilterCategory("Hàn Quốc", "han-quoc"),
        FilterCategory("Nhật Bản", "nhat-ban"),
        FilterCategory("Âu Mỹ", "au-my"),
        FilterCategory("Việt Nam", "viet-nam")
    )
}
