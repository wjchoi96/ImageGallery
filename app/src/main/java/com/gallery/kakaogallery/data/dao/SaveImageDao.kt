package com.gallery.kakaogallery.data.dao

import com.gallery.kakaogallery.data.entity.local.ImageEntity
import com.gallery.kakaogallery.domain.model.SearchImageModel
import kotlinx.coroutines.flow.Flow

interface SaveImageDao {
    suspend fun fetchSaveImages(): Flow<List<ImageEntity>>

    fun removeImages(idxList: List<Int>)

    fun saveImages(image: List<SearchImageModel>, saveDateTimeMill: Long)
}