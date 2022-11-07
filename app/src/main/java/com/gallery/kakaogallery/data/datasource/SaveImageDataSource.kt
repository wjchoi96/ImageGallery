package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.entity.local.ImageEntity
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.SearchImageModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface SaveImageDataSource {
    fun fetchSaveImages(): Observable<List<ImageEntity>>

    fun removeImages(idxList: List<Int>): Completable

    fun saveImages(image: List<SearchImageModel>, saveDateTimeMill: Long): Completable
}