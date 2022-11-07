package com.gallery.kakaogallery.data.entity.remote.response


import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.SearchImageModel
import com.google.gson.annotations.SerializedName

data class VideoSearchResponse(
    @SerializedName("documents")
    val documents: List<Document>,
    @SerializedName("meta")
    val meta: Meta
) {
    data class Document(
        @SerializedName("author")
        val author: String,
        @SerializedName("datetime")
        val datetime: String,
        @SerializedName("play_time")
        val playTime: Int,
        @SerializedName("thumbnail")
        val thumbnail: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("url")
        val url: String
    ) {
        fun toModel(
            dateTimeToShow: String,
            dateTimeMill: Long
        ): SearchImageModel = SearchImageModel(
            dateTimeToShow = dateTimeToShow,
            dateTimeMill = dateTimeMill,
            imageUrl = thumbnail,
            thumbnailUrl = null,
            imageType = ImageModel.ImageType.Video
        )
    }

    data class Meta(
        @SerializedName("is_end")
        val isEnd: Boolean,
        @SerializedName("pageable_count")
        val pageableCount: Int,
        @SerializedName("total_count")
        val totalCount: Int
    )
}