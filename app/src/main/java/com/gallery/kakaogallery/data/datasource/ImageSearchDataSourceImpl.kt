package com.gallery.kakaogallery.data.datasource

import android.util.Log
import com.gallery.kakaogallery.data.constant.SearchConstant
import com.gallery.kakaogallery.data.entity.remote.request.ImageSearchRequest
import com.gallery.kakaogallery.data.entity.remote.response.ImageSearchResponse
import com.gallery.kakaogallery.data.service.ImageSearchService
import com.gallery.kakaogallery.domain.model.Result
import com.gallery.kakaogallery.domain.model.ResultError
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class ImageSearchDataSourceImpl(
    private val searchImageApi : ImageSearchService
): ImageSearchDataSource {
    companion object {
        private const val TAG = "ImageSearchDataSourceImpl"
    }
    private var imagePageable = true

    override fun hasNextPage(): Boolean = imagePageable

    override fun fetchImageQueryRes(
        query: String,
        page: Int
    ): Observable<Result<List<ImageSearchResponse.Document>>> {
        if(page == 1)
            imagePageable = true
        return when(imagePageable){
            false -> Observable.just(Result.Fail(ResultError.MaxPage))
            true -> {
                searchImageApi.run {
                    this.requestSearchImage(
                        query,
                        ImageSearchRequest.SortType.Recency.key,
                        page, // 1~50
                        SearchConstant.ImagePageSizeMaxValue
                    ).subscribeOn(Schedulers.computation())
                        .map {
                            when {
                                it.meta != null -> {
                                    imagePageable = !it.meta.isEnd
                                    Result.Success(it.documents)
                                }
                                else -> Result.Fail(ResultError.Fail)
                            }
                        }
                        .onErrorReturn {
                            it.printStackTrace()
                            Log.e(TAG, "onErrorReturn images search res")
                            Result.Fail(ResultError.Crash)
                        }
                        .toObservable()
                }
            }
        }
    }
}