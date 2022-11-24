package com.gallery.kakaogallery.domain.model

import java.io.Serializable

data class GalleryImageModel(
    override val dateTimeToShow: String,
    override val imageUrl: String,
    override val thumbnailUrl: String?,
    override val imageType: ImageType,
    override val isSelect: Boolean = false
) : ImageModel(), Serializable {
    companion object {
        val Empty: GalleryImageModel
            get() = GalleryImageModel("", "", null, ImageType.Image)
    }
    override val hash: String = imageUrl + dateTimeToShow

    override val imageThumbUrl: String
        get() = thumbnailUrl ?: imageUrl
    override val isImageType: Boolean
        get() = imageType == ImageType.Image

    infix fun isSameItem(other: Any?): Boolean {
        return when (other) {
            is GalleryImageModel -> this.hash == other.hash
            else -> false
        }
    }

    infix fun isSameContent(other: Any?): Boolean {
        return when (other) {
            is GalleryImageModel -> this == other
            else -> false
        }
    }
}
