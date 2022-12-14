package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.entity.remote.response.ImageSearchResponse
import kotlinx.coroutines.flow.Flow

interface ImageSearchDataSource {
    suspend fun fetchImageQueryRes(query: String, page: Int): Flow<List<ImageSearchResponse.Document>>
}