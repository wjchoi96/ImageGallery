package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.ImageListTypeModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import com.gallery.kakaogallery.domain.util.GalleryDateConvertUtil
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

class SaveSelectImageUseCase(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(selectImageUrlMap: MutableMap<String, Int>, images: List<ImageListTypeModel>): Single<Boolean>{
        return Completable.defer{
            val saveImages = mutableListOf<ImageModel>()
            for(selectIdx in selectImageUrlMap.values){
                val saveTimeMill = Date().time
                saveImages.add(
                    (images[selectIdx] as ImageListTypeModel.Image).let {
                        it.copy(image = it.image.copy(
                            saveTimeMill = saveTimeMill,
                            saveDateTimeToShow = GalleryDateConvertUtil.convertToPrint(saveTimeMill),
                            isSelect = false
                        ))
                    }.image
                )
            }
            imageRepository.saveImages(saveImages)
        }.subscribeOn(Schedulers.computation())
            .toSingle {
                true
            }
            .doOnError {
                it.printStackTrace()
            }
    }
}