package com.gallery.kakaogallery.domain.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object GalleryDateConvertUtil {
    private const val galleryRawDateFormatString = "yyyy-MM-dd'T'hh:mm:ss" // api 에서 리턴되는 형식
    private const val galleryViewDateFormatString = "yyyy.MM.dd HH:mm:ss" // view 출력 형식

    private val rawFormatter = SimpleDateFormat(galleryRawDateFormatString).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val viewFormatter = SimpleDateFormat(galleryViewDateFormatString).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun convertToMill(dateStr: String): Long? {
        return try{
            rawFormatter.parse(dateStr)?.time
        }catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun convertToPrint(dateStr: String): String? {
        return try{
            viewFormatter.format(rawFormatter.parse(dateStr)!!)
        }catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}