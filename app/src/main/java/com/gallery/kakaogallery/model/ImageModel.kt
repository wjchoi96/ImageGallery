package com.gallery.kakaogallery.model

import android.util.Log
import com.google.gson.annotations.SerializedName
import java.lang.Exception
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
    val dateTimeToShow : String,
    val dateTimeMill : Long?,
    val imageUrl : String,
    private val thumbnailUrl : String?,
    var saveDateTime : String? = null,
    var saveTimeMill : Long? = null,
    var isSelect : Boolean = false
){
    companion object {
        val Empty : ImageModel = ImageModel("", 0, "", null)
    }
    val isImageType : Boolean
        get() = thumbnailUrl.isNullOrBlank()
    val imageThumbUrl : String
        get() = thumbnailUrl ?: imageUrl
    val isSaveImage : Boolean
        get() = saveDateTime != null && saveTimeMill != null

    fun setRemovedImage() {
        saveDateTime = null
        saveTimeMill = null
    }
    fun setSaveDateTime() : String {
        saveTimeMill = Date().time
        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        saveDateTime = formatter.format(Date(saveTimeMill!!))
        Log.d("Test", "saveDateTime : $saveDateTime")
        return saveDateTime!!
    }

    fun toMinString() : String {
        return "dateTime : $dateTimeToShow, isSaveImage : ${isSaveImage}, imageUrl : $imageUrl\nisSelect : $isSelect, saveDateTime : $saveDateTime, saveTimeMill : $saveTimeMill"
    }

    override fun equals(other: Any?): Boolean {
        if(other !is ImageModel){
            return false
        }
        return other.dateTimeToShow == dateTimeToShow &&
                other.saveDateTime == saveDateTime &&
                other.saveTimeMill == saveTimeMill &&
                other.isSelect == isSelect &&
                other.imageUrl == imageUrl &&
                other.imageThumbUrl == imageThumbUrl

    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    class Payload(
        val changedIdx : List<Int>,
        val payloadType : PayloadType,
        val changedPayload : ChangedType? = null
    ) {
        enum class PayloadType {
            NewList,
            Inserted,
            InsertedRange,
            Removed,
            Changed
        }
        enum class ChangedType {
            Save, Select
        }
    }
}