package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.entity.remote.response.ImageSearchResponse
import io.reactivex.rxjava3.core.Single

interface ImageSearchDataSource {
    fun fetchImageQueryRes(query: String, page: Int): Single<List<ImageSearchResponse.Document>>
}