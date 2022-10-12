package com.gallery.kakaogallery

import android.content.Context
import androidx.core.content.edit
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication

class KakaoGallerySharedPreferences(
    private val context: Context = KakaoGalleryApplication.instance
) {
    companion object {
        private const val versionCode = "1.0.1"
        private const val fileName = "kakaoGallery_$versionCode"
    }
    private val sp = context.getSharedPreferences(fileName, 0)

    var savedImageList: String
        get() = sp.getString(Key.SavedImageList.spKey, null) ?: ""
        set(value) = sp.edit { putString(Key.SavedImageList.spKey, value) }

    private enum class Key(val spKey : String) {
        SavedImageList("sp_key_saved_image_list")
    }
}