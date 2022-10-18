package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.entity.remote.response.VideoSearchResponse
import io.reactivex.rxjava3.core.Observable

interface VideoSearchDataSource {
    fun fetchVideoQueryRes(query : String, page : Int): Observable<List<VideoSearchResponse.Document>>
}