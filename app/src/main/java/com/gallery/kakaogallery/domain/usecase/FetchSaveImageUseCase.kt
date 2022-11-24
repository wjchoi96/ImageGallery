package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.GalleryImageModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class FetchSaveImageUseCase(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(): Observable<Result<List<GalleryImageModel>>> {
        return imageRepository
            .fetchSaveImages()
            .observeOn(Schedulers.computation())
            .map {
                Result.success(it)
            }
            .onErrorReturn {
                it.printStackTrace()
                println("error debug at useCase => $it")
                Result.failure(it)
            }
    }
}