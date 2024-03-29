package com.gallery.kakaogallery.domain.model

sealed class SearchImageListTypeModel {
    data class Query(val query: String?) : SearchImageListTypeModel()
    data class Image(val image: SearchImageModel) : SearchImageListTypeModel()
    object Skeleton : SearchImageListTypeModel()

    enum class ViewType(val id: Int) {
        Query(0),
        Image(1),
        Skeleton(2)
    }

    infix fun isSameItem(other: Any?): Boolean {
        return when {
            this is Query && other is Query -> true // query 가 변경되면 payload 로 수정되게 하기 위해 true 리턴
            this is Image && other is Image -> this.image isSameItem other.image
            this is Skeleton && other is Skeleton -> true
            else -> false
        }
    }

    infix fun isSameContent(other: Any?): Boolean {
        return when {
            this is Query && other is Query -> true // query 가 변경되면 payload 로 수정되게 하기 위해 true 리턴
            this is Image && other is Image -> this.image isSameContent other.image
            this is Skeleton && other is Skeleton -> true
            else -> false
        }
    }
}