package com.gallery.kakaogallery.domain.repository

import com.gallery.kakaogallery.domain.model.ImageModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface ImageRepository {
    fun fetchQueryData(query: String, page: Int): Single<List<ImageModel>>

    fun fetchSaveImages(): Observable<List<ImageModel>>

    fun removeImages(idxList: List<Int>): Observable<Boolean>

    fun saveImages(image: List<ImageModel>): Observable<Boolean>
}