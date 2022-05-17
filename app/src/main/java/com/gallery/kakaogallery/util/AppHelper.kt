package com.gallery.kakaogallery.util

import android.app.Application

class AppHelper : Application() {
    companion object {
        private const val TAG = "KakaoGallery"
        fun <T> getTag(cls : Class<T>) : String {
            return "$TAG :: ${cls.name}" // name => package 포함 / simple name => package 미포함
        }

        var isOnline : Boolean = true

        lateinit var instance : Application
        lateinit var mRetrofit : RetrofitManager
        lateinit var dataStorage: DataStorage
    }
    private var netWorkUtil : NetworkUtil? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        mRetrofit = RetrofitManager.instance
        dataStorage = DataStorage.instance

        netWorkUtil = NetworkUtil.instance
        netWorkUtil?.register()
    }

    override fun onTerminate() {
        super.onTerminate()
        netWorkUtil?.unregister()
    }

}
