package com.gallery.kakaogallery.data.entity.remote.request

//https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide#search-image
class ImageSearchRequest internal constructor(
    val query: String,
    val sort: SortType,
    val page: Int, // 결과 페이지 번호, 1~50 사이의 값, 기본 값 1
    val pageSize: Int // 한 페이지에 보여질 문서 수, 1~50 사이의 값, 기본 값 10
) {
    enum class SortType(val key: String) {
        Accuracy("accuracy"), // 정확도순
        Recency("recency") // 최신순
    }
}
