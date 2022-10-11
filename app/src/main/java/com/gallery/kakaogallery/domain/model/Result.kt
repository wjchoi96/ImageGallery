package com.gallery.kakaogallery.domain.model

sealed class Result<T>(
    val data : T? = null,
    val error : ResultError? = null
) {
    class Success<T>(data : T) : Result<T>(data)
    class Fail<T>(error: ResultError) : Result<T>(null, error)
}

/**
 * ResultError 가 들어갈수 있는, code, message 만 있으면 어떠한 에러든 들어갈 수 있는 에러 객체 생성
 */
data class KakaoGalleryError(
    val code : Int,
    val message : String
){

}

enum class ResultError(val code : Int, val message : String) {
    Fail(0, ""),
    Crash(1, ""),
    MaxPage(2, "")
}
