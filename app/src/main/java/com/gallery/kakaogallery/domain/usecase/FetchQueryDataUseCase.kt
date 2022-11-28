package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.SearchImageListTypeModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class FetchQueryDataUseCase(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(
        query: String,
        page: Int,
        skeletonSize: Int = 15
    ): Observable<Result<List<SearchImageListTypeModel>>> {
        return if(query.isBlank()){
            Observable.just(Result.success(listOf(SearchImageListTypeModel.Query(query))))
        } else {
            val fetchQueryStream = imageRepository
                .fetchQueryData(query, page)
                .observeOn(Schedulers.computation())
                .toObservable()
                .map {
                    Result.success(
                        when(page){
                            1 -> listOf(SearchImageListTypeModel.Query(query)) +
                                    it.map { image -> SearchImageListTypeModel.Image(image) }
                            else -> it.map { image -> SearchImageListTypeModel.Image(image) }
                        }
                    )
                }
                .onErrorResumeNext {
                    it.printStackTrace()
                    println("error debug at useCase => $it")
                    Observable.create { emitter ->
                        emitter.onNext(Result.success(listOf(SearchImageListTypeModel.Query(query))))
                        emitter.onNext(Result.failure(it))
                        emitter.onComplete()
                    }
                }

            return when (page) {
                1 -> fetchQueryStream
                        .delay(500L, TimeUnit.MILLISECONDS) // skeleton ui 를 잘 보여주기 위한 delay
                        .startWithItem(
                            Result.success(
                                MutableList(skeletonSize + 1) { i ->
                                    when (i) {
                                        0 -> SearchImageListTypeModel.Query(query)
                                        else -> SearchImageListTypeModel.Skeleton
                                    }
                                }
                            )
                        )
                else -> fetchQueryStream
            }
        }
    }
}