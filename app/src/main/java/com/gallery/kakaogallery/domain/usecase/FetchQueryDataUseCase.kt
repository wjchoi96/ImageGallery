package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.SearchImageListTypeModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class FetchQueryDataUseCase(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(query: String, page: Int): Single<List<ImageListTypeModel>> {
        return if(query.isBlank()){
            Single.just(listOf(ImageListTypeModel.Query(query)))
        } else {
            imageRepository
                .fetchQueryData(query, page)
                .observeOn(Schedulers.computation())
                .map {
                    when(page){
                        1 -> listOf(ImageListTypeModel.Query(query)) +
                                it.map { image -> ImageListTypeModel.Image(image) }
                        else -> it.map { image -> ImageListTypeModel.Image(image) }
                    }
                }
                .doOnError {
                    it.printStackTrace()
                    println("error debug at useCase => $it")
                }
        }
    }
}