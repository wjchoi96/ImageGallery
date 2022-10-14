package com.gallery.kakaogallery.domain.repository

import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.Result
import io.reactivex.rxjava3.core.Observable

interface ImageRepository {
    fun fetchQueryData(query: String, page: Int): Observable<Result<List<ImageModel>>>

    fun fetchSaveImages(): Observable<List<ImageModel>>

    fun removeImages(idxList: List<Int>): Observable<Boolean>

    fun saveImage(image: ImageModel): Observable<Boolean>
}