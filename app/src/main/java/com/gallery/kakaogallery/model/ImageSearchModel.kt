package com.gallery.kakaogallery.model

import android.util.Log
import com.gallery.kakaogallery.ApiAddressConstant
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.core.Flowable
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

interface ImageSearchService {
    @GET(ApiAddressConstant.Image.ImageSearch)
    fun requestSearchImage(
        @Query("query") query : String,
        @Query("sort") sort : String,
        @Query("page") page : Int, // 결과 페이지 번호, 1~50 사이의 값, 기본 값 1
        @Query("size") pageSize : Int //한 페이지에 보여질 문서 수, 1~80 사이의 값, 기본 값 80
    ) : Flowable<ImageSearchResModel>
}

//https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide#search-image
class ImageSearchReqModel internal constructor(
    val query : String,
    val sort : SortType,
    val page : Int, // 결과 페이지 번호, 1~50 사이의 값, 기본 값 1
    val pageSize : Int // 한 페이지에 보여질 문서 수, 1~50 사이의 값, 기본 값 10
){
    enum class SortType(val key : String) {
        Accuracy("accuracy"), // 정확도순
        Recency("recency") // 최신순
    }
}

class ImageSearchResModel {
    @SerializedName("meta")
    var imageSearchMetaData : ImageSearchMetaModel? = null
    @SerializedName("documents")
    var imageSearchResList : ArrayList<ImageSearchModel> ? = null

    fun isApiResSuccess() : Boolean {
        return imageSearchResList != null && imageSearchMetaData != null
    }
}

class ImageSearchMetaModel internal constructor(
    @SerializedName("totle_count")val totalCount : Int, // 검색된 문서 수
    @SerializedName("pageable_count")val pageableCount : Int, // total_count 중 노출 가능 문서 수
    @SerializedName("is_end")val isEnd : Boolean // 현재 페이지가 마지막 페이지인지 여부, 값이 false면 page를 증가시켜 다음 페이지를 요청할 수 있음
)

data class ImageSearchModel internal constructor(
    @SerializedName("datetime")private val dateTime : String, // [YYYY]-[MM]-[DD]T[hh]:[mm]:[ss].000+[tz] => 2022-03-18T20:00:04.000+09:00
    @SerializedName("image_url")override val imageUrl : String,
    @SerializedName("thumbnail_url")val thumbnailUrl : String,
    @SerializedName("width")val width : Int,
    @SerializedName("height")val height : Int,
    @SerializedName("display_sitename")val siteName : String, // 출처
    @SerializedName("doc_url")val docUrl : String // 문서 url
) : QuerySearchModel() {
    override val dateTimeStr: String
        get() = dateTime
    override val imageThumbnailUrl: String
        get() = thumbnailUrl
    fun toMinString() : String {
        return "image => url : $imageUrl\ndate : $dateTime, mill : $dateTimeMill}"
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
