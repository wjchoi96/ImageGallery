package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.entity.remote.response.VideoSearchResponse
import kotlinx.coroutines.flow.Flow

interface VideoSearchDataSource {
    fun fetchVideoQueryRes(query: String, page: Int): Flow<List<VideoSearchResponse.Document>>
}