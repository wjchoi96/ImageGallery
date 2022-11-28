package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.GalleryImageListTypeModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class FetchSaveImageUseCase(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(skeletonSize: Int = 15): Observable<Result<List<GalleryImageListTypeModel>>> {
        return imageRepository
            .fetchSaveImages()
            .observeOn(Schedulers.computation())
            .map<Result<List<GalleryImageListTypeModel>>> {
                val list = it.map { image ->
                    GalleryImageListTypeModel.Image(image)
                }
                Result.success(list)
            }
            .onErrorResumeNext {
                it.printStackTrace()
                println("error debug at useCase => $it")
                Observable.create { emitter ->
                    emitter.onNext(Result.success(emptyList()))
                    emitter.onNext(Result.failure(it))
                    emitter.onComplete()
                }
            }
            .delay(500L, TimeUnit.MILLISECONDS)
            .startWithItem(
                Result.success(MutableList(skeletonSize) { GalleryImageListTypeModel.Skeleton })
            )
    }
}