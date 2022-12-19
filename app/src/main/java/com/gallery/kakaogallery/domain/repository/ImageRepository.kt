package com.gallery.kakaogallery.domain.repository

import com.gallery.kakaogallery.domain.model.GalleryImageModel
import com.gallery.kakaogallery.domain.model.SearchImageModel
import io.reactivex.rxjava3.core.Completable
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    suspend fun fetchQueryData(query: String, page: Int): Flow<List<SearchImageModel>>

    suspend fun fetchSaveImages(): Flow<List<GalleryImageModel>>

    fun removeImages(idxList: List<Int>): Completable

    fun saveImages(image: List<SearchImageModel>, saveDateTimeMill: Long): Flow<Boolean>
}