package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.domain.model.ImageModel
import io.reactivex.rxjava3.core.Observable

interface SaveImageDataSource {
    fun fetchSaveImages(): Observable<List<ImageModel>>

    fun removeImages(idxList: List<Int>): Observable<Boolean>

    fun saveImage(image: ImageModel): Observable<Boolean>
}