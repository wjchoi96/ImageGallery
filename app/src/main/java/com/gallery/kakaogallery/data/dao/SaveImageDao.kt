package com.gallery.kakaogallery.data.dao

import com.gallery.kakaogallery.data.entity.local.ImageEntity
import com.gallery.kakaogallery.domain.model.SearchImageModel
import kotlinx.coroutines.flow.Flow

interface SaveImageDao {
    fun fetchSaveImages(): Flow<List<ImageEntity>>

    suspend fun removeImages(idxList: List<Int>)

    suspend fun saveImages(image: List<SearchImageModel>, saveDateTimeMill: Long)
}