package com.gallery.kakaogallery.domain.repository

import com.gallery.kakaogallery.domain.model.GalleryImageModel
import com.gallery.kakaogallery.domain.model.SearchImageModel
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    fun fetchQueryData(query: String, page: Int): Flow<List<SearchImageModel>>

    fun fetchSaveImages(): Flow<List<GalleryImageModel>>

    fun removeImages(idxList: List<Int>): Flow<Boolean>

    fun saveImages(image: List<SearchImageModel>, saveDateTimeMill: Long): Flow<Boolean>
}