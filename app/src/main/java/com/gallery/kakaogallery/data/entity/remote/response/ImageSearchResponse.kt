package com.gallery.kakaogallery.data.entity.remote.response


import com.gallery.kakaogallery.domain.model.ImageModel
import com.google.gson.annotations.SerializedName

data class ImageSearchResponse(
    @SerializedName("documents")
    val documents: List<Document>,
    @SerializedName("meta")
    val meta: Meta
) {
    data class Document(
        @SerializedName("collection")
        val collection: String,
        @SerializedName("datetime")
        val datetime: String,
        @SerializedName("display_sitename")
        val displaySiteName: String,
        @SerializedName("doc_url")
        val docUrl: String,
        @SerializedName("height")
        val height: Int,
        @SerializedName("image_url")
        val imageUrl: String,
        @SerializedName("thumbnail_url")
        val thumbnailUrl: String,
        @SerializedName("width")
        val width: Int
    ){
        fun toModel(
            dateTimeToShow: String,
            dateTimeMill: Long
        ): ImageModel = ImageModel(
            dateTimeToShow = dateTimeToShow,
            dateTimeMill = dateTimeMill,
            imageUrl = imageUrl,
            thumbnailUrl = thumbnailUrl
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