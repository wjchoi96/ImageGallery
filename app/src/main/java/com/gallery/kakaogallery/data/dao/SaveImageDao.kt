package com.gallery.kakaogallery.data.dao

import com.gallery.kakaogallery.domain.model.ImageModel
import io.reactivex.rxjava3.core.Observable

interface SaveImageDao {
    fun fetchSaveImages(): Observable<List<ImageModel>>

    fun removeImages(idxList: List<Int>): Boolean

    fun saveImages(image: List<ImageModel>): Boolean
}