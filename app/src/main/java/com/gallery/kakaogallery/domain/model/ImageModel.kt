package com.gallery.kakaogallery.domain.model

import java.text.SimpleDateFormat
import java.util.*

/*
    1. dateTime => 출력 가능한 형태로 저장
    2. dateTimeMill => 비교할일 없음 => x
    3. saveTime => String? => 출력 가능한 형태로 저장
    4. saveTimeMill => 저장할때만 필요 -> 가지고있자

    => isSave -> saveTime 이 있는지 없는지로 체크하자

    url : String
    thumbnail url : String

 */
data class ImageModel internal constructor(
    val dateTimeToShow: String,
    val dateTimeMill: Long?,
    val imageUrl: String,
    private val thumbnailUrl: String?,
    var saveDateTimeToShow: String? = null,
    var saveTimeMill: Long? = null,
    var isSelect: Boolean = false
) {
    companion object {
        val Empty: ImageModel = ImageModel("", 0, "", null)
    }

    val hash: String = imageUrl + if(saveDateTimeToShow.isNullOrBlank()) dateTimeToShow else saveDateTimeToShow

    val isImageType: Boolean
        get() = thumbnailUrl.isNullOrBlank()
    val imageThumbUrl: String
        get() = thumbnailUrl ?: imageUrl
    val isSaveImage: Boolean
        get() = saveDateTimeToShow != null && saveTimeMill != null

    fun setRemovedImage() {
        saveDateTimeToShow = null
        saveTimeMill = null
    }

    fun toMinString(): String {
        return "dateTime : $dateTimeToShow, isSaveImage : ${isSaveImage}, imageUrl : $imageUrl\nisSelect : $isSelect, saveDateTime : $saveDateTimeToShow, saveTimeMill : $saveTimeMill"
    }
    
}