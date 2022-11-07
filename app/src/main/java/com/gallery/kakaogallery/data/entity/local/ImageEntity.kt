package com.gallery.kakaogallery.data.entity.local

import com.gallery.kakaogallery.domain.model.GalleryImageModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.SearchImageModel

data class ImageEntity(
    val imageUrl: String,
    private val thumbnailUrl: String?,
    val saveDateTimeMill: Long,
    val imageDateTimeMill: Long?,
    val isImageType: Boolean
) {
    companion object {
        val Empty: ImageEntity
            get() = ImageEntity("", null, 0L, null, true)

        fun from(
            searchImageModel: SearchImageModel,
            saveDateTimeMill: Long
        ) = ImageEntity(
            imageUrl = searchImageModel.imageUrl,
            thumbnailUrl = searchImageModel.imageThumbUrl,
            saveDateTimeMill = saveDateTimeMill,
            imageDateTimeMill = searchImageModel.dateTimeMill,
            isImageType = searchImageModel.isImageType
        )
    }
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
