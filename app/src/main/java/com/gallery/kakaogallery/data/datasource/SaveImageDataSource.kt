package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.entity.local.ImageEntity
import com.gallery.kakaogallery.domain.model.SearchImageModel
import kotlinx.coroutines.flow.Flow

interface SaveImageDataSource {
    suspend fun fetchSaveImages(): Flow<List<ImageEntity>>

    fun removeImages(idxList: List<Int>): Flow<Boolean>

    fun saveImages(image: List<SearchImageModel>, saveDateTimeMill: Long): Flow<Boolean>
}