package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.SearchImageListTypeModel
import com.gallery.kakaogallery.domain.model.SearchImageModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

class SaveSelectImageUseCase(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(selectImageUrlMap: MutableMap<String, Int>, images: List<SearchImageListTypeModel>): Single<Boolean>{
        return Completable.defer{
            val saveImages = mutableListOf<SearchImageModel>()
            for(selectIdx in selectImageUrlMap.values){
                saveImages.add(
                    (images[selectIdx] as SearchImageListTypeModel.Image).let {
                        it.copy(image = it.image.copy(
                            isSelect = false
                        ))
                    }.image
                )
            }
            imageRepository.saveImages(saveImages, Date().time)
        }.subscribeOn(Schedulers.computation())
            .toSingle {
                true
            }
            .doOnError {
                it.printStackTrace()
            }
    }
}