package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.repository.ImageRepository
import com.gallery.kakaogallery.presentation.di.IODispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class RemoveSaveImageUseCase(
    private val imageRepository: ImageRepository,
    @IODispatcher private val dispatcher: CoroutineDispatcher
) {
    operator fun invoke(selectImageHashMap: MutableMap<String, Int>): Flow<Result<Boolean>> {
        return imageRepository.removeImages(selectImageHashMap.values.toList())
            .map {
                Result.success(it)
            }.catch {
                it.printStackTrace()
                emit(Result.failure(it))
            }.flowOn(dispatcher)

    }
}