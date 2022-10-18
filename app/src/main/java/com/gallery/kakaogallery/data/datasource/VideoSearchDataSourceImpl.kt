package com.gallery.kakaogallery.data.datasource

import android.util.Log
import com.gallery.kakaogallery.data.constant.SearchConstant
import com.gallery.kakaogallery.data.entity.remote.request.VideoSearchRequest
import com.gallery.kakaogallery.data.entity.remote.response.VideoSearchResponse
import com.gallery.kakaogallery.data.service.VideoSearchService
import com.gallery.kakaogallery.domain.model.MaxPageException
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class VideoSearchDataSourceImpl @Inject constructor(
    private val searchVideoApi : VideoSearchService
): VideoSearchDataSource {
    companion object {
        private const val TAG = "VideoSearchDataSourceImpl"
    }
    private var videoPageable = true

    override fun fetchVideoQueryRes(
        query: String,
        page: Int
    ): Observable<List<VideoSearchResponse.Document>> {
        if(page == 1)
            videoPageable = true
        return when(videoPageable){
            false -> {
                Log.d(TAG,"error debug => throw MaxPageException")
                Observable.error { MaxPageException() }
            }
            true -> {
                searchVideoApi.requestSearchVideo(
                    query,
                    VideoSearchRequest.SortType.Recency.key,
                    page, // 1~50
                    SearchConstant.VideoPageSizeMaxValue
                ).observeOn(AndroidSchedulers.mainThread())
                    .map {
                        Log.d(TAG, "Video mapping run at ${Thread.currentThread().name}")
                        videoPageable = !it.meta.isEnd
                        it.documents
                    }
                    .onErrorResumeNext {
                        it.printStackTrace()
                        Log.d(TAG,"error debug => after api response => $it")
                        Flowable.error{ it }
                    }
                    .toObservable()
            }
        }
    }
}