package com.gallery.kakaogallery.domain.model

abstract class ImageModel {

    enum class ImageType {
        Image, Video
    }

    abstract val dateTimeToShow: String
    abstract val imageUrl: String
    protected abstract val thumbnailUrl: String?
    abstract val imageType: ImageType
    open var isSelect: Boolean = false

    abstract val hash: String
    abstract val isImageType: Boolean
    abstract val imageThumbUrl: String

    fun toMinString(): String {
        return "dateTime : $dateTimeToShow, imageUrl : $imageUrl\nisSelect : $isSelect"
    }
    
}