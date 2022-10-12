package com.gallery.kakaogallery

import android.content.Context
import androidx.core.content.edit
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class KakaoGallerySharedPreferences @Inject constructor(
    @ApplicationContext context: Context
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