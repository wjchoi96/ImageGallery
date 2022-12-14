package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.SearchImageListTypeModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class FetchQueryDataUseCase(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(
        query: String,
        page: Int,
        skeletonSize: Int = 15
    ): Flow<Result<List<SearchImageListTypeModel>>> {
        return if(query.isBlank()){
            flow {
                emit(Result.success(listOf(SearchImageListTypeModel.Query(query))))
            }
        } else {
            val fetchQueryFlow = imageRepository
                .fetchQueryData(query, page)
                .map {
                    Result.success(
                        when(page){
                            1 -> listOf(SearchImageListTypeModel.Query(query)) +
                                    it.map { image -> SearchImageListTypeModel.Image(image) }
                            else -> it.map { image -> SearchImageListTypeModel.Image(image) }
                        }
                    )
                }.flowOn(Dispatchers.Default)

            return when (page) {
                1 -> fetchQueryFlow
                    .onStart {
                        emit(
                            Result.success(
                                MutableList(skeletonSize + 1) { i ->
                                    when (i) {
                                        0 -> SearchImageListTypeModel.Query(query)
                                        else -> SearchImageListTypeModel.Skeleton
                                    }
                                }
                            )
                        )
                        delay(500) // skeleton ui 를 잘 보여주기 위한 delay
                    }
                    .catch {
                        it.printStackTrace()
                        println("error debug at useCase => $it")
                        emit(Result.success(listOf(SearchImageListTypeModel.Query(query))))
                        emit(Result.failure(it))
                    }
                else -> fetchQueryFlow
                    .catch {
                        it.printStackTrace()
                        println("error debug at useCase => $it")
                        emit(Result.failure(it))
                    }
            }
        }
    }
}