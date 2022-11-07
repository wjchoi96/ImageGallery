package com.gallery.kakaogallery.domain.model

import java.io.Serializable

data class SearchImageModel(
    override val dateTimeToShow: String,
    val dateTimeMill: Long,
    override val imageUrl: String,
    override val thumbnailUrl: String?,
    override val imageType: ImageType,
    override var isSelect: Boolean = false
) : ImageModel(), Serializable {
    companion object {
        val Empty: SearchImageModel
            get() = SearchImageModel("", 0L, "", null, ImageType.Image)
    }
    override val hash: String = imageUrl + dateTimeToShow

    override val imageThumbUrl: String
        get() = thumbnailUrl ?: imageUrl
    override val isImageType: Boolean
        get() = imageType == ImageType.Image
}


