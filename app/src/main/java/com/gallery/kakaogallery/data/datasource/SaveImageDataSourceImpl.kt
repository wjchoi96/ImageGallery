package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.dao.SaveImageDao
import com.gallery.kakaogallery.data.entity.local.ImageEntity
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.SearchImageModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class SaveImageDataSourceImpl @Inject constructor(
    private val saveImageDao: SaveImageDao
) : SaveImageDataSource {

    override fun fetchSaveImages(): Observable<List<ImageEntity>> {
        return saveImageDao.fetchSaveImages()
            .subscribeOn(Schedulers.io())
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