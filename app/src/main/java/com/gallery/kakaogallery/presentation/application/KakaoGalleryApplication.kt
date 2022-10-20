package com.gallery.kakaogallery.presentation.application

import android.app.Application
import com.gallery.kakaogallery.BuildConfig
import com.gallery.kakaogallery.presentation.network.NetworkUtil
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class KakaoGalleryApplication : Application() {
    companion object {
        var isOnline: Boolean = true

        lateinit var instance: Application
    }

    private var netWorkUtil: NetworkUtil? = null

    override fun onCreate() {
        super.onCreate()
        instance = this

        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        netWorkUtil = NetworkUtil.instance
        netWorkUtil?.register()
    }

    override fun onTerminate() {
        super.onTerminate()
        netWorkUtil?.unregister()
    }

}
