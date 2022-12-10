package com.gallery.kakaogallery.domain.model

import java.io.Serializable

sealed class GalleryImageListTypeModel : Serializable {
    data class Image(val image: GalleryImageModel): GalleryImageListTypeModel()
    object Skeleton : GalleryImageListTypeModel()

    enum class ViewType(val id: Int) {
        Image(0),
        Skeleton(1)
    }

    infix fun isSameItem(other: Any?): Boolean {
        return when {
            this is Image && other is Image -> this.image isSameItem other.image
            this is Skeleton && other is Skeleton -> true
            else -> false
        }
    }

    infix fun isSameContent(other: Any?): Boolean {
        return when {
            this is Image && other is Image -> this.image isSameContent other.image
            this is Skeleton && other is Skeleton -> true
            else -> false
        }
    }
}