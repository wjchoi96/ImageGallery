package com.gallery.kakaogallery.presentation.application

import android.app.Application
import com.gallery.kakaogallery.presentation.network.NetworkUtil
import com.gallery.kakaogallery.presentation.network.RetrofitManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KakaoGalleryApplication : Application() {
    companion object {
        private const val TAG = "KakaoGallery"
        fun <T> getTag(cls : Class<T>) : String {
            return "$TAG :: ${cls.name}" // name => package 포함 / simple name => package 미포함
        }

        var isOnline : Boolean = true

        lateinit var instance : Application
        lateinit var mRetrofit : RetrofitManager
    }
    private var netWorkUtil : NetworkUtil? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        mRetrofit = RetrofitManager.instance

        netWorkUtil = NetworkUtil.instance
        netWorkUtil?.register()
    }

    override fun onTerminate() {
        super.onTerminate()
        netWorkUtil?.unregister()
    }

}
