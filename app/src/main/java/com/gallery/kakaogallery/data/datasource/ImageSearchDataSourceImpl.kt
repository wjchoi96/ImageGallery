package com.gallery.kakaogallery.data.datasource

import com.gallery.kakaogallery.data.constant.SearchConstant
import com.gallery.kakaogallery.data.entity.remote.request.ImageSearchRequest
import com.gallery.kakaogallery.data.entity.remote.response.ImageSearchResponse
import com.gallery.kakaogallery.data.service.ImageSearchService
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.gallery.kakaogallery.presentation.di.IODispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

class ImageSearchDataSourceImpl @Inject constructor(
    private val searchImageApi: ImageSearchService,
    @IODispatcher private val dispatcher: CoroutineDispatcher
) : ImageSearchDataSource {

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
    ): Flow<List<ImageSearchResponse.Document>> {
        if (page == 1)
            imagePageable = true
        return when (imagePageable) {
            false -> {
                flow {
                    Timber.d("error debug => throw MaxPageException")
                    throw MaxPageException()
                }
            }
            true -> flow {
                emit(
                    searchImageApi.requestSearchImage(
                        query,
                        ImageSearchRequest.SortType.Recency.key,
                        page, // 1~50
                        SearchConstant.ImagePageSizeMaxValue
                    )
                )
            }.map {
                Timber.d("Image mapping run at " + Thread.currentThread().name)
                imagePageable = !it.meta.isEnd
                it.documents
            }.catch {
                it.printStackTrace()
                Timber.d("error debug => after api response => $it")
                throw it
            }.flowOn(dispatcher)
        }
    }
}