package com.gallery.kakaogallery.util

import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication

class DataStorage private constructor() {
    companion object {
        const val emptyValue = SPUtil.emptyValue
        val instance = DataStorage()
    }
    private var spUtil: SPUtil = SPUtil.instance

    private enum class Key(val spKey : String) {
        SavedImageList("sp_key_saved_iamge_list")
    }

    var savedImageList: String
        get() = spUtil.getStringValue(Key.SavedImageList.spKey)
        set(value) = spUtil.setStringValue(Key.SavedImageList.spKey, value)


    private class SPUtil private constructor(){
        companion object {
            const val emptyValue = "sp_empty"
            private const val versionCode = "1.0.1"
            private const val fileName = "kakaoGallery_$versionCode"
            val instance = SPUtil()
        }
        private val pref = KakaoGalleryApplication.instance.getSharedPreferences(fileName, 0)

        fun getStringValue(spKey : String) : String {
            return pref.getString(spKey,
                emptyValue
            )!!
        }
        fun setStringValue(spKey : String, value : String){
            pref.edit().putString(spKey,value).apply()
        }

        fun getIntValue(spKey : String) : Int {
            return pref.getInt(spKey,0)
        }
        fun setIntValue(spKey : String, value : Int) {
            pref.edit().putInt(spKey,value).apply()
        }

        fun getBooleanValueDefaultFalse(spKey : String) : Boolean {
            return pref.getBoolean(spKey,false)
        }
        fun getBooleanValueDefaultTrue(spKey : String) : Boolean {
            return pref.getBoolean(spKey,true)
        }
        fun setBooleanValue(spKey : String, value : Boolean) {
            pref.edit().putBoolean(spKey,value).apply()
        }

    }
}