package com.gallery.kakaogallery.data.datasource

import android.util.Log
import com.gallery.kakaogallery.data.constant.SearchConstant
import com.gallery.kakaogallery.data.entity.remote.request.ImageSearchRequest
import com.gallery.kakaogallery.data.entity.remote.response.ImageSearchResponse
import com.gallery.kakaogallery.data.service.ImageSearchService
import com.gallery.kakaogallery.domain.model.Result
import com.gallery.kakaogallery.domain.model.ResultError
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class ImageSearchDataSourceImpl @Inject constructor(
    private val searchImageApi : ImageSearchService
): ImageSearchDataSource {
    companion object {
        private const val TAG = "ImageSearchDataSourceImpl"
    }
    private var imagePageable = true

    /**
     * Retrofit 의 RxSupport 기능에서
     * subscribeOn을 통해 지정한 스케쥴러는, 네트워크 호출을 준비하기 위한 코드에만 적용이되고,
     * 실제 네트워크 호출 코드는 Rx 에서 지정한 스케쥴러가 적용이 된다
     * 때문에, 해당 스케쥴러로 다운스트림이 실행되는 것
     *
     * 참고: https://stackoverflow.com/questions/28462839/using-subscribeon-with-retrofit
     */
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
                    ).observeOn(AndroidSchedulers.mainThread())
                        .map {
                            Log.d(TAG, "Image mapping run at ${Thread.currentThread().name}")
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