package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.ImageListTypeModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class FetchQueryDataUseCase(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(query: String, page: Int): Single<Result<List<ImageListTypeModel>>> {
        return if(query.isBlank()){
            Single.just(Result.success(listOf(ImageListTypeModel.Query(query))))
        } else {
            imageRepository
                .fetchQueryData(query, page)
                .observeOn(Schedulers.computation())
                .map {
                    when(page){
                        1 -> Result.success(
                            listOf(ImageListTypeModel.Query(query)) +
                                    it.map { image -> ImageListTypeModel.Image(image) }
                        )
                        else -> Result.success(
                            it.map { image -> ImageListTypeModel.Image(image) }
                        )
                    }
                }
                .onErrorReturn {
                    it.printStackTrace()
                    println("error debug at useCase => $it")
                    Result.failure(it)
                }
        }
    }
}