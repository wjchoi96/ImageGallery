package com.gallery.kakaogallery.domain.model

abstract class ImageModel {

    enum class ImageType {
        Image, Video
    }

    abstract val dateTimeToShow: String
    abstract val imageUrl: String
    protected abstract val thumbnailUrl: String?
    abstract val imageType: ImageType
    abstract val isSelect: Boolean

    abstract val hash: String
    abstract val isImageType: Boolean
    abstract val imageThumbUrl: String

    fun toMinString(): String {
        return "dateTime : $dateTimeToShow, imageUrl : $imageUrl\nisSelect : $isSelect"
    }
    
}