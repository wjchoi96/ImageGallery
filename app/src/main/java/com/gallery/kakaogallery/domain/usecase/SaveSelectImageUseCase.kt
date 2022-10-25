package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import com.gallery.kakaogallery.domain.util.GalleryDateConvertUtil
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

class SaveSelectImageUseCase(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(selectImageUrlMap: MutableMap<String, Int>, images: List<ImageModel>): Observable<Result<Boolean>>{
        return Observable.defer{
            val saveImages = mutableListOf<ImageModel>()
            for(selectIdx in selectImageUrlMap.values){
                val saveTimeMill = Date().time
                saveImages.add(images[selectIdx].copy(
                    saveTimeMill = saveTimeMill,
                    saveDateTimeToShow = GalleryDateConvertUtil.convertToPrint(saveTimeMill),
                    isSelect = false
                ))
            }
            imageRepository.saveImages(saveImages)
        }.subscribeOn(Schedulers.computation())
            .map {
                Result.success(it)
            }
            .onErrorReturn {
                it.printStackTrace()
                Result.failure(it)
            }
    }
}