package com.gallery.kakaogallery.presentation.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel

class SplashViewModel : ViewModel() {

    private val splashDuration: Long = 500
    var isReady = false
        private set

    init {
        Handler(Looper.getMainLooper()).postDelayed({ isReady = true }, splashDuration)
    }

}