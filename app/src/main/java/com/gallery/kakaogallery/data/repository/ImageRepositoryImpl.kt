package com.gallery.kakaogallery.data.repository

import android.util.Log
import com.gallery.kakaogallery.data.datasource.ImageSearchDataSource
import com.gallery.kakaogallery.data.datasource.SaveImageDataSource
import com.gallery.kakaogallery.data.datasource.VideoSearchDataSource
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.UnKnownException
import com.gallery.kakaogallery.domain.repository.ImageRepository
import com.gallery.kakaogallery.domain.util.GalleryDateConvertUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

/**
 * 이미지 검색, 비디오 검색 2가지를 repository 에서 메소드 화 하고
 * useCase 에서 merge?
 */
class ImageRepositoryImpl @Inject constructor(
    private val imageSearchDataSource : ImageSearchDataSource,
    private val videoSearchDataSource: VideoSearchDataSource,
    private val saveImageDataSource: SaveImageDataSource
): ImageRepository {
    companion object {
        private const val TAG = "ImageRepository"
    }

    private fun fetchImageQuery(query: String, page: Int): Observable<List<ImageModel>> {
        return imageSearchDataSource.fetchImageQueryRes(query, page)
            .observeOn(Schedulers.computation())
            .map {
                it.map { data ->
                    data.toModel(
                        dateTimeToShow = GalleryDateConvertUtil.convertToPrint(data.datetime) ?: "",
                        dateTimeMill = GalleryDateConvertUtil.convertToMill(data.datetime) ?: 0L
                    )
                }
            }
    }

    private fun fetchVideoQuery(query: String, page: Int): Observable<List<ImageModel>> {
        return videoSearchDataSource.fetchVideoQueryRes(query, page)
            .observeOn(Schedulers.computation())
            .map {
                it.map { data ->
                    data.toModel(
                        dateTimeToShow = GalleryDateConvertUtil.convertToPrint(data.datetime) ?: "",
                        dateTimeMill = GalleryDateConvertUtil.convertToMill(data.datetime) ?: 0L
                    )
                }
            }
    }

    // zip 은 가장 최근에 zip 되지 않은 데이터들끼리 zip 을 한다
    // 두개의 api 중 하나만 성공하고, 하나만 실패하는경우 다음 검색의 결과값에 이전 결과값의 데이터가 남아서 영향을 주게 되므로 api 에러가 뜨는 경우 빈 데이터를 넣어서 onNext 해준다
    // BiFunction 의 작업 환경은 첫번째 Stream 스케쥴러를 따라간다
    override fun fetchQueryData(query: String, page: Int): Observable<List<ImageModel>> {
        return Observable.zip(
            fetchImageQuery(query, page)
                .observeOn(Schedulers.computation())
                .wrapResult(),
            fetchVideoQuery(query, page)
                .wrapResult()
        ) { t1, t2 ->
            when {
                t1.isFailure && t2.isFailure -> throw t1.exceptionOrNull() ?: t2.exceptionOrNull() ?: UnKnownException()
                else -> {
                    Log.d(TAG, "Observable.zip run at ${Thread.currentThread().name}")
                    (t1.getOrNull() ?: emptyList()) + (t2.getOrNull() ?: emptyList()).run {
                        sortedByDescending { it.dateTimeMill }
                    }
                }
            }
        }.subscribeOn(Schedulers.io())
            .onErrorResumeNext {
                it.printStackTrace()
                Log.d(TAG, "error debug => after zip => $it")
                Observable.error{ it }
            }
    }

    private fun Observable<List<ImageModel>>.wrapResult(): Observable<Result<List<ImageModel>>> =
        this.map {
            Result.success(it)
        }.onErrorReturn {
            Result.failure(it)
        }

    override fun fetchSaveImages(): Observable<List<ImageModel>> {
        return saveImageDataSource.fetchSaveImages()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun removeImages(idxList: List<Int>): Observable<Boolean> {
        return saveImageDataSource.removeImages(idxList)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun saveImage(image: ImageModel): Observable<Boolean> {
        return saveImageDataSource.saveImage(image)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

}
