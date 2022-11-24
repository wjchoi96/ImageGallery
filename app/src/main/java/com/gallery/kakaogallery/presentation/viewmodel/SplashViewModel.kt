package com.gallery.kakaogallery.presentation.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel

class SplashViewModel : ViewModel() {

    val splashDuration: Long = 1000
    var isReady = false
        private set

    init {
        Handler(Looper.getMainLooper()).postDelayed({ isReady = true }, splashDuration)
    }
}