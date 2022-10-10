package com.gallery.kakaogallery.util

import android.util.Log
import com.gallery.kakaogallery.data.remote.request.ImageSearchRequest
import com.gallery.kakaogallery.data.remote.request.VideoSearchRequest
import com.gallery.kakaogallery.data.remote.response.ImageSearchResponse
import com.gallery.kakaogallery.data.remote.response.VideoSearchResponse
import com.gallery.kakaogallery.data.remote.service.ImageSearchService
import com.gallery.kakaogallery.data.remote.service.VideoSearchService
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers

object ApiUtil : BaseUtil() {
    private val mRetrofit = KakaoGalleryApplication.mRetrofit

    object Image {
        fun requestSearchImage(reqModel : ImageSearchRequest) : Flowable<ImageSearchResponse>{
            return mRetrofit.getService(ImageSearchService::class.java).run {
                Log.d("TAG", "api request : ${Thread.currentThread().name}")
                this.requestSearchImage(
                    reqModel.query,
                    reqModel.sort.key,
                    reqModel.page,
                    reqModel.pageSize
                )
            }.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    object Video {
        fun requestSearchVideo(reqModel : VideoSearchRequest) : Flowable<VideoSearchResponse>{
            return mRetrofit.getService(VideoSearchService::class.java).run {
                this.requestSearchVideo(
                    reqModel.query,
                    reqModel.sort.key,
                    reqModel.page,
                    reqModel.pageSize
                ).subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
            }
        }
    }
}