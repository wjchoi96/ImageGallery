package com.gallery.kakaogallery.domain.repository

import com.gallery.kakaogallery.domain.model.GalleryImageModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.SearchImageModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface ImageRepository {
    fun fetchQueryData(query: String, page: Int): Single<List<SearchImageModel>>

    fun fetchSaveImages(): Observable<List<GalleryImageModel>>

    fun removeImages(idxList: List<Int>): Completable

    fun saveImages(image: List<SearchImageModel>, saveDateTimeMill: Long): Completable
}