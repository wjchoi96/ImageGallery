package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.dao.SaveImageDao
import com.gallery.kakaogallery.data.entity.local.ImageEntity
import com.gallery.kakaogallery.domain.model.SearchImageModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SaveImageDataSourceImpl @Inject constructor(
    private val saveImageDao: SaveImageDao
) : SaveImageDataSource {

    override suspend fun fetchSaveImages(): Flow<List<ImageEntity>> {
        return saveImageDao.fetchSaveImages()
            .flowOn(Dispatchers.IO)
    }

    override fun removeImages(idxList: List<Int>): Completable {
        return Completable.fromCallable {
            saveImageDao.removeImages(idxList)
        }.subscribeOn(Schedulers.io())
    }

    override fun saveImages(image: List<SearchImageModel>, saveDateTimeMill: Long): Completable {
        return Completable.fromCallable {
            saveImageDao.saveImages(image, saveDateTimeMill)
        }.subscribeOn(Schedulers.io())
    }
}