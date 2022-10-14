package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.entity.remote.response.VideoSearchResponse
import com.gallery.kakaogallery.domain.model.Result
import io.reactivex.rxjava3.core.Observable

interface VideoSearchDataSource {
    fun fetchVideoQueryRes(query : String, page : Int): Observable<Result<List<VideoSearchResponse.Document>>>
}