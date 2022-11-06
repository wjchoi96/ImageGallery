package com.gallery.kakaogallery.data.entity.local

import com.gallery.kakaogallery.domain.model.GalleryImageModel
import com.gallery.kakaogallery.domain.model.ImageModel

data class ImageEntity(
    val imageUrl: String,
    private val thumbnailUrl: String?,
    val saveDateTimeMill: Long,
    val imageDateTimeMill: Long?,
    val isImageType: Boolean
) {
    fun toModel(
        dateTimeToShow: String
    ): GalleryImageModel {
        return GalleryImageModel(
            dateTimeToShow = dateTimeToShow,
            imageUrl = imageUrl,
            thumbnailUrl = thumbnailUrl,
            imageType = if(isImageType) ImageModel.ImageType.Image else ImageModel.ImageType.Video
        )
    }
}
