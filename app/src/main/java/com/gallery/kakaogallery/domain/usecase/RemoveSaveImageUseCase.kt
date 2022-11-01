package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.reactivex.rxjava3.core.Single

class RemoveSaveImageUseCase(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(selectImageHashMap: MutableMap<String, Int>): Single<Result<Boolean>> {
        return imageRepository.removeImages(selectImageHashMap.values.toList())
            .toSingle {
                Result.success(true)
            }
            .onErrorReturn {
                it.printStackTrace()
                Result.failure(it)
            }
    }
}