package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.entity.remote.response.ImageSearchResponse
import com.gallery.kakaogallery.domain.model.Result
import io.reactivex.rxjava3.core.Observable

interface ImageSearchDataSource {
    fun hasNextPage(): Boolean

    fun fetchImageQueryRes(query : String, page : Int): Observable<Result<List<ImageSearchResponse.Document>>>
}