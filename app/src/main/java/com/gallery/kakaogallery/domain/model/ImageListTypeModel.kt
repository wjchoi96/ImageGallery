package com.gallery.kakaogallery.domain.model

import java.io.Serializable

sealed class ImageListTypeModel : Serializable {
    data class Query(val query: String?): ImageListTypeModel()
    data class Image(val image: SearchImageModel): ImageListTypeModel()

    enum class ViewType(val id: Int) {
        Query(0),
        Image(1)
    }

    infix fun isSameItem(other: Any?): Boolean {
        return when {
            this is Query && other is Query -> true // query 가 변경되면 payload 로 수정되게 하기 위해 true 리턴
            this is Image && other is Image -> this.image.hash == other.image.hash
            else -> false
        }
    }

    infix fun isSameContent(other: Any?): Boolean {
        return when {
            this is Query && other is Query -> true // query 가 변경되면 payload 로 수정되게 하기 위해 true 리턴
            this is Image && other is Image -> this.image == other.image
            else -> false
        }
    }
}