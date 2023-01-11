package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.SearchImageListTypeModel
import com.gallery.kakaogallery.domain.model.SearchImageModel
import com.gallery.kakaogallery.domain.model.UnKnownException
import com.gallery.kakaogallery.domain.repository.ImageRepository
import com.gallery.kakaogallery.presentation.di.IODispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*

@FlowPreview
class SaveSelectImageUseCase(
    private val imageRepository: ImageRepository,
    @IODispatcher private val dispatcher: CoroutineDispatcher
) {
    operator fun invoke(selectImageUrlMap: MutableMap<String, Int>, images: List<SearchImageListTypeModel>): Flow<Result<Boolean>>{
        return flow<Result<List<SearchImageModel>>> {
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
            emit(Result.success(saveImages))
        }.catch {
            it.printStackTrace()
            emit(Result.failure(it))
        }.flatMapConcat {
            when {
                it.isSuccess -> imageRepository.saveImages(it.getOrThrow(), Date().time)
                else -> throw it.exceptionOrNull() ?: UnKnownException()
            }
        }.map {
            Result.success(it)
        }.catch {
            it.printStackTrace()
            emit(Result.failure(it))
        }.flowOn(dispatcher)
    }
}