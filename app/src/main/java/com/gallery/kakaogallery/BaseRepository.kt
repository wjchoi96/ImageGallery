package com.gallery.kakaogallery

import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication

abstract class BaseRepository {
    protected val TAG = KakaoGalleryApplication.getTag(this::class.java)
}
