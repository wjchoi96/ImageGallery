package com.gallery.kakaogallery.data.repository

import android.util.Log
import com.gallery.kakaogallery.data.datasource.ImageSearchDataSource
import com.gallery.kakaogallery.data.datasource.SaveImageDataSource
import com.gallery.kakaogallery.data.datasource.VideoSearchDataSource
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.Result
import com.gallery.kakaogallery.domain.repository.ImageRepository
import com.gallery.kakaogallery.domain.util.GalleryDateConvertUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val imageSearchDataSource : ImageSearchDataSource,
    private val videoSearchDataSource: VideoSearchDataSource,
    private val saveImageDataSource: SaveImageDataSource
): ImageRepository {
    companion object {
        private const val TAG = "ImageRepository"
    }

    override fun hasNextPage(): Boolean {
        return imageSearchDataSource.hasNextPage() || videoSearchDataSource.hasNextPage()
    }

    // zip 은 가장 최근에 zip 되지 않은 데이터들끼리 zip 을 한다
    // 두개의 api 중 하나만 성공하고, 하나만 실패하는경우 다음 검색의 결과값에 이전 결과값의 데이터가 남아서 영향을 주게 되므로 api 에러가 뜨는 경우 빈 데이터를 넣어서 onNext 해준다
    override fun fetchQueryData(query: String, page: Int): Observable<Result<List<ImageModel>>> {
        return Observable.zip(
            imageSearchDataSource.fetchImageQueryRes(query, page)
                .observeOn(Schedulers.computation()), // BiFunction 의 작업 환경은 첫번째 Stream 스케쥴러를 따라간다
            videoSearchDataSource.fetchVideoQueryRes(query, page),
            BiFunction { t1, t2 ->
                when {
                    t1 is Result.Fail && t2 is Result.Fail -> return@BiFunction Result.Fail(t1.error!!)
                    else -> {
                        Log.d(TAG, "Observable.zip run at ${Thread.currentThread().name}")
                        val searchList = ((t1.data ?: emptyList()).map{
                            it.toModel(
                                dateTimeToShow = GalleryDateConvertUtil.convertToPrint(it.datetime) ?: "",
                                dateTimeMill = GalleryDateConvertUtil.convertToMill(it.datetime) ?: 0L
                            )
                        } + (t2.data ?: emptyList()).map{
                            it.toModel(
                                dateTimeToShow = GalleryDateConvertUtil.convertToPrint(it.datetime) ?: "",
                                dateTimeMill = GalleryDateConvertUtil.convertToMill(it.datetime) ?: 0L
                            )
                        }).run { sortedByDescending { it.dateTimeMill } }
                        return@BiFunction Result.Success(searchList)
                    }
                }
            }
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
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
