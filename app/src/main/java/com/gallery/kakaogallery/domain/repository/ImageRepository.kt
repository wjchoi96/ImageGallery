package com.gallery.kakaogallery.domain.repository

import com.gallery.kakaogallery.domain.model.GalleryImageModel
import com.gallery.kakaogallery.domain.model.SearchImageModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    suspend fun fetchQueryData(query: String, page: Int): Flow<List<SearchImageModel>>

    fun fetchSaveImages(): Observable<List<GalleryImageModel>>

    fun removeImages(idxList: List<Int>): Completable

    fun saveImages(image: List<SearchImageModel>, saveDateTimeMill: Long): Completable
}