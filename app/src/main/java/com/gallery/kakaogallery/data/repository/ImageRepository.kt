package com.gallery.kakaogallery.data.repository

import android.util.Log
import com.gallery.kakaogallery.Result
import com.gallery.kakaogallery.ResultError
import com.gallery.kakaogallery.SearchConstant
import com.gallery.kakaogallery.data.SaveImageStorage
import com.gallery.kakaogallery.data.remote.request.ImageSearchRequest
import com.gallery.kakaogallery.data.remote.request.VideoSearchRequest
import com.gallery.kakaogallery.data.remote.response.ImageSearchModel
import com.gallery.kakaogallery.data.remote.response.VideoSearchModel
import com.gallery.kakaogallery.data.remote.service.ImageSearchService
import com.gallery.kakaogallery.data.remote.service.VideoSearchService
import com.gallery.kakaogallery.model.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Repo 에 대한 글
 * https://bperhaps.tistory.com/entry/Repository%EC%99%80-Dao%EC%9D%98-%EC%B0%A8%EC%9D%B4%EC%A0%90
 *
 * Repository는 영구저장소를 의미하는게 아니다. 말 그대로, 객체의 상태를 관리하는 저장소일 뿐이다. 즉, Repository의 구현이 파일시스템으로 되든, 아니면 HashMap으로 구현됐든지 상관없다. 그냥 객체(entity)에 대한 CRUD를 수행할 수 있으면 된다.
 * CRUD => create, read, update ,delete
 */
class ImageRepository(
    private val searchImageApi : ImageSearchService,
    private val searchVideoApi : VideoSearchService,
    private val saveImageDao : SaveImageStorage
) : BaseRepository() {
    private var imagePageable = true
    private var videoPageable = true

    /**
     * 이미지, 비디오 둘다 다음 페이지가 없다면 false 리턴
     */
    fun hasNextPage() : Boolean {
        return imagePageable || videoPageable
    }


    // zip 은 가장 최근에 zip 되지 않은 데이터들끼리 zip 을 한다
    // 두개의 api 중 하나만 성공하고, 하나만 실패하는경우 다음 검색의 결과값에 이전 결과값의 데이터가 남아서 영향을 주게 되므로 api 에러가 뜨는 경우 빈 데이터를 넣어서 onNext 해준다
    fun fetchQueryData(query: String, page: Int): Observable<Result<List<ImageModel>>> {
        if(page == 1) {
            imagePageable = true
            videoPageable = true
        }
        return Observable.zip(
            fetchImageQueryRes(query, page),
            fetchVideoQueryRes(query, page),
            BiFunction { t1, t2 ->
                if(t1 is Result.Fail && t2 is Result.Fail){
                    return@BiFunction Result.Fail(t1.error!!)
                }
                // 2개 리스트를 합치고, sort 한다 => comparator 을 사용해서
                // 1. dateTimeMill 순으로
                Log.d(TAG, "fetch list merge : ${t1.data?.size}, ${t2.data?.size} - thread check[${Thread.currentThread().name}]")
                Log.d(TAG, "fetch list merge => \nt1 : ${t1.data?.firstOrNull()}\nt2 : ${t2.data?.firstOrNull()}")
                var searchList = (t1.data ?: ArrayList()).map{
//                    Log.d(TAG, "map image : $it")
                    it.toImageModel()
                } + (t2.data ?: ArrayList()).map{
//                    Log.d(TAG, "map video : $it")
                    it.toImageModel()
                }
                searchList = searchList.sortedByDescending { it.dateTimeMill }
                return@BiFunction Result.Success(searchList)
            }
        ).observeOn(AndroidSchedulers.mainThread())
    }
    private fun QuerySearchModel.toImageModel() : ImageModel {
        return ImageModel(dateTimeToShow, dateTimeMill, imageUrl, imageThumbnailUrl)
    }

    private fun fetchImageQueryRes(query : String, page : Int): Observable<Result<ArrayList<ImageSearchModel>>>{
        return if(!imagePageable) {
            Observable.just(Result.Fail(ResultError.MaxPage))
        }else {
            searchImageApi.run {
                this.requestSearchImage(
                    query,
                    ImageSearchRequest.SortType.Recency.key,
                    page, // 1~50
                    SearchConstant.ImagePageSizeMaxValue
                ).subscribeOn(Schedulers.computation())
                    .doOnNext {
                        Log.d(TAG, "doOnNext image search res : ${it.isApiResSuccess()}")
                    }
                    .map {
                        when {
                            it.isApiResSuccess() && it.imageSearchMetaData != null && it.imageSearchResList != null -> {
                                imagePageable = !(it.imageSearchMetaData?.isEnd ?: true)
                                Result.Success(it.imageSearchResList!!)
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

    private fun fetchVideoQueryRes(query : String, page : Int): Observable<Result<ArrayList<VideoSearchModel>>>{
        return if(!videoPageable) {
            Observable.just(Result.Fail(ResultError.MaxPage))
        }else {
            return searchVideoApi.run {
                this.requestSearchVideo(
                    query,
                    VideoSearchRequest.SortType.Recency.key,
                    page, // 1~50
                    SearchConstant.VideoPageSizeMaxValue
                ).subscribeOn(Schedulers.computation())
                    .doOnNext {
                        Log.d(TAG, "doOnNext video search res : ${it.isApiResSuccess()} - thread check[${Thread.currentThread().name}]")
                    }
                    .map {
                        when {
                            it.isApiResSuccess() && it.videoSearchMetaData != null && it.videoSearchResList != null -> {
                                videoPageable = !(it.videoSearchMetaData?.isEnd ?: true)
                                Result.Success(it.videoSearchResList!!)
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
