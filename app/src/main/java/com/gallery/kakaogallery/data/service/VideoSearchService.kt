package com.gallery.kakaogallery.data.service

import com.gallery.kakaogallery.data.constant.ApiAddressConstant
import com.gallery.kakaogallery.data.entity.remote.response.VideoSearchResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface VideoSearchService {
    @GET(ApiAddressConstant.Video.VideoSearch)
    fun requestSearchVideo(
        @Query("query") query: String,
        @Query("sort") sort: String,
        @Query("page") page: Int, // 결과 페이지 번호, 1~50 사이의 값, 기본 값 1
        @Query("size") pageSize: Int // 한 페이지에 보여질 문서 수, 1~50 사이의 값, 기본 값 10
    ): Single<VideoSearchResponse>
}