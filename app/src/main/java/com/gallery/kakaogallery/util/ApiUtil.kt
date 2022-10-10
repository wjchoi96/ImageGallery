package com.gallery.kakaogallery.util

import android.util.Log
import com.gallery.kakaogallery.model.*
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers

object ApiUtil : BaseUtil() {
    private val mRetrofit = KakaoGalleryApplication.mRetrofit

    object Image {
        fun requestSearchImage(reqModel : ImageSearchReqModel) : Flowable<ImageSearchResModel>{
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
        fun requestSearchVideo(reqModel : VideoSearchReqModel) : Flowable<VideoSearchResModel>{
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