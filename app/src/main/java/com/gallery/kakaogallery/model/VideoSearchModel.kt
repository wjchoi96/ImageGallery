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


interface VideoSearchService {
    @GET(ApiAddressConstant.Video.VideoSearch)
    fun requestSearchVideo(
        @Query("query") query : String,
        @Query("sort") sort : String,
        @Query("page") page : Int, // 결과 페이지 번호, 1~50 사이의 값, 기본 값 1
        @Query("size") pageSize : Int // 한 페이지에 보여질 문서 수, 1~50 사이의 값, 기본 값 10
    ) : Flowable<VideoSearchResModel>
}

//https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide#search-image
class VideoSearchReqModel internal constructor(
    val query : String,
    val sort : SortType,
    val page : Int, // 결과 페이지 번호, 1~15 사이의 값
    val pageSize : Int // 한 페이지에 보여질 문서 수, 1~30 사이의 값, 기본 값 15
){
    enum class SortType(val key : String) {
        Accuracy("accuracy"), // 정확도순
        Recency("recency") // 최신순
    }
}

class VideoSearchResModel {
    @SerializedName("meta")
    var videoSearchMetaData : VideoSearchMetaModel? = null
    @SerializedName("documents")
    var videoSearchResList : ArrayList<VideoSearchModel>? = null

    fun isApiResSuccess() : Boolean {
        return videoSearchResList != null && videoSearchMetaData != null
    }
}

class VideoSearchMetaModel internal constructor(
    @SerializedName("totle_count")val totalCount : Int, // 검색된 문서 수
    @SerializedName("pageable_count")val pageableCount : Int, // total_count 중 노출 가능 문서 수
    @SerializedName("is_end")val isEnd : Boolean // 현재 페이지가 마지막 페이지인지 여부, 값이 false면 page를 증가시켜 다음 페이지를 요청할 수 있음
)

data class VideoSearchModel internal constructor(
    @SerializedName("datetime")private val dateTime : String, // [YYYY]-[MM]-[DD]T[hh]:[mm]:[ss].000+[tz]
    @SerializedName("contents")val content : String,
    @SerializedName("title")val title : String,
    @SerializedName("url")val videoUrl : String,
    @SerializedName("play_tile")val playTime : Int, // 초 단위
    @SerializedName("thumbnail")val videoThumbnailUrl : String,
    @SerializedName("author")val author : String // 업로더
) : QuerySearchModel() {
    override val dateTimeStr: String
        get() = dateTime
    override val imageUrl: String
        get() = videoThumbnailUrl
    override val imageThumbnailUrl: String? = null
    fun toMinString() : String {
        return "video => thumbUrl : $videoThumbnailUrl\ndate : $dateTime, mill : $dateTimeMill"
    }
}
