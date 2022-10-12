package com.gallery.kakaogallery.data.datasource

import android.util.Log
import com.gallery.kakaogallery.data.constant.SearchConstant
import com.gallery.kakaogallery.data.entity.remote.request.VideoSearchRequest
import com.gallery.kakaogallery.data.entity.remote.response.VideoSearchResponse
import com.gallery.kakaogallery.data.service.VideoSearchService
import com.gallery.kakaogallery.domain.model.Result
import com.gallery.kakaogallery.domain.model.ResultError
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

class VideoSearchDataSourceImpl(
    private val searchVideoApi : VideoSearchService
): VideoSearchDataSource {
    companion object {
        private const val TAG = "VideoSearchDataSourceImpl"
    }
    private var videoPageable = true

    override fun hasNextPage(): Boolean = videoPageable

    override fun fetchVideoQueryRes(
        query: String,
        page: Int
    ): Observable<Result<List<VideoSearchResponse.Document>>> {
        if(page == 1)
            videoPageable = true
        return when(videoPageable){
            false -> Observable.just(Result.Fail(ResultError.MaxPage))
            true -> {
                searchVideoApi.run {
                    this.requestSearchVideo(
                        query,
                        VideoSearchRequest.SortType.Recency.key,
                        page, // 1~50
                        SearchConstant.VideoPageSizeMaxValue
                    ).observeOn(AndroidSchedulers.mainThread())
                        .map {
                            Log.d(TAG, "Video mapping run at ${Thread.currentThread().name}")
                            when {
                                it.documents != null -> {
                                    videoPageable = !it.meta.isEnd
                                    Result.Success(it.documents)
                                }
                                else -> Result.Fail(ResultError.Fail)
                            }
                        }
                        .onErrorReturn {
                            it.printStackTrace()
                            Log.e(TAG, "onErrorReturn video search res")
                            Result.Fail(ResultError.Crash)
                        }
                        .toObservable()
                }
            }
        }
    }
}