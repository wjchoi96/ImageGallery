package com.gallery.kakaogallery.model

import android.util.Log
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

abstract class QuerySearchModel{
    val dateTimeMill: Long?
        get() = convertDateToDateTimeMill()
    val dateTimeToShow: String
        get() = getPrintDateTime()
    abstract val imageUrl : String
    abstract val imageThumbnailUrl : String?
    protected abstract val dateTimeStr : String

    private var _dateTimeMill : Long? = null
    private var _dateTimeToShow : String? = null

    // api 23 을 지원하느라 api26부터 지원되는 DateTimeFormatter 이나, LocalDate 를 사용하지 않았다
    private fun convertDateToDateTimeMill() : Long? {
        // +09:00 => UTC 표준 시간보다 9시간 + 해줬다는 의미 => 한국시
        // .000 그냥 문자열로 붙인거
        // T date 와 time 사이에 붙이는거
        if(_dateTimeMill != null)
            return _dateTimeMill
        _dateTimeMill = parseDateToMill(dateTimeStr)
        return _dateTimeMill
    }
    private fun getPrintDateTime() : String {
        if(_dateTimeToShow != null)
            return _dateTimeToShow!!
        _dateTimeToShow = if(dateTimeMill != null){
            parseMillToDateStr(dateTimeMill!!)
        }else{
            convertDateToDateTimeMill()
            parseMillToDateStr(dateTimeMill ?: return dateTimeStr)
        }
        return _dateTimeToShow ?: dateTimeStr
    }

    private fun parseDateToMill(dateTime : String) : Long? {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")
            val date = parser.parse(dateTime) ?: return null
            val millValue = date.time
//            val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm:ss") // for debug
//            Log.d("convertDateToDateTimeMill test", "${millValue}=>${formatter.format(Date(millValue))}")
            millValue
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun parseMillToDateStr(mill : Long) : String {
        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm:ss") // for debug
        return formatter.format(Date(mill))
    }

}
/*
     한국은 UTC+9시간대에 속하므로 UTC 시간에 +9를 해주면 한국 환산 시간이 나온다
     ISO 8601 시간대 => 설명 : https://java119.tistory.com/24
     [YYYY]-[MM]-[DD]T[hh]:[mm]:[ss].000+[tz]
     =>  2022-03-18T20:00:04.000+09:00
     2022-03-18 T 20:00:04 .000 +09:00

     +09:00 =>  UTC기준 시로부터 9시간 +된 시간 => 한국시간임을 알림
     T : 날짜 뒤에 시간이 오는것을 표시해주는 문자입니다.

 */
