package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.dao.SaveImageDao
import com.gallery.kakaogallery.domain.model.ImageModel
import io.reactivex.rxjava3.core.Observable

class SaveImageDataSourceImpl(
    private val saveImageDao: SaveImageDao
): SaveImageDataSource {

    override fun fetchSaveImages(): Observable<List<ImageModel>> {
        return saveImageDao.fetchSaveImages()
    }

    override fun removeImages(idxList: List<Int>): Boolean {
        return saveImageDao.removeImages(idxList)
    }

    override fun saveImage(image: ImageModel): Boolean {
        return saveImageDao.saveImage(image)
    }
}