package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.dao.SaveImageDao
import com.gallery.kakaogallery.data.entity.local.ImageEntity
import com.gallery.kakaogallery.domain.model.SearchImageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SaveImageDataSourceImpl @Inject constructor(
    private val saveImageDao: SaveImageDao
) : SaveImageDataSource {

    override suspend fun fetchSaveImages(): Flow<List<ImageEntity>> {
        return saveImageDao.fetchSaveImages()
            .flowOn(Dispatchers.IO)
    }

    override fun removeImages(idxList: List<Int>): Flow<Boolean> = flow {
        saveImageDao.removeImages(idxList)
        emit(true)
    }.flowOn(Dispatchers.IO)

    override fun saveImages(
        image: List<SearchImageModel>,
        saveDateTimeMill: Long
    ): Flow<Boolean> = flow {
        saveImageDao.saveImages(image, saveDateTimeMill)
        emit(true)
    }.flowOn(Dispatchers.IO)
}