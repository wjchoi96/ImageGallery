package com.gallery.kakaogallery.util

import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication

abstract class BaseUtil {
    protected val TAG = KakaoGalleryApplication.getTag(this::class.java)
}