package com.gallery.kakaogallery.domain.usecase

import com.gallery.kakaogallery.domain.model.GalleryImageListTypeModel
import com.gallery.kakaogallery.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class FetchSaveImageUseCase(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(skeletonSize: Int = 15): Flow<Result<List<GalleryImageListTypeModel>>> {
        return imageRepository
            .fetchSaveImages()
            .map {
                val list = it.map { image ->
                    GalleryImageListTypeModel.Image(image) as GalleryImageListTypeModel
                }
                Result.success(list)
            }
            .onStart {
                emit(Result.success(MutableList(skeletonSize) { GalleryImageListTypeModel.Skeleton }))
                delay(500)
            }
            .catch {
                it.printStackTrace()
                emit(Result.success(emptyList()))
                emit(Result.failure(it))
            }
            .flowOn(Dispatchers.Default)
    }
}