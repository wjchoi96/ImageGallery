package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.reactivex.rxjava3.core.Observable

class RemoveSaveImageUseCase(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(selectImageHashMap: MutableMap<String, Int>): Observable<Result<Boolean>> {
        return imageRepository.removeImages(selectImageHashMap.values.toList())
            .map {
                Result.success(it)
            }.onErrorReturn {
                it.printStackTrace()
                Result.failure(it)
            }
    }
}