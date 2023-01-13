package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.constant.SearchConstant
import com.gallery.kakaogallery.data.entity.remote.request.VideoSearchRequest
import com.gallery.kakaogallery.data.entity.remote.response.VideoSearchResponse
import com.gallery.kakaogallery.data.service.VideoSearchService
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.gallery.kakaogallery.presentation.di.IODispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

class VideoSearchDataSourceImpl @Inject constructor(
    private val searchVideoApi: VideoSearchService,
    @IODispatcher private val dispatcher: CoroutineDispatcher
) : VideoSearchDataSource {

    private var videoPageable = true

    override fun fetchVideoQueryRes(
        query: String,
        page: Int
    ): Flow<List<VideoSearchResponse.Document>> {
        if (page == 1)
            videoPageable = true
        return when (videoPageable) {
            false -> {
                flow {
                    Timber.d("error debug => throw MaxPageException")
                    throw MaxPageException()
                }
            }
            true -> flow {
                emit(
                    searchVideoApi.requestSearchVideo(
                        query,
                        VideoSearchRequest.SortType.Recency.key,
                        page, // 1~50
                        SearchConstant.VideoPageSizeMaxValue
                    )
                )
            }.map {
                Timber.d("Video mapping run at " + Thread.currentThread().name)
                videoPageable = !it.meta.isEnd
                it.documents
            }.catch {
                it.printStackTrace()
                Timber.d("error debug => after api response => $it")
                throw it
            }.flowOn(dispatcher)
        }
    }
}