package com.gallery.kakaogallery.data.dao

import com.gallery.kakaogallery.data.entity.local.ImageEntity
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.SearchImageModel
import io.reactivex.rxjava3.core.Observable

interface SaveImageDao {
    fun fetchSaveImages(): Observable<List<ImageEntity>>

    fun removeImages(idxList: List<Int>)

    fun saveImages(image: List<SearchImageModel>, saveDateTimeMill: Long)
}