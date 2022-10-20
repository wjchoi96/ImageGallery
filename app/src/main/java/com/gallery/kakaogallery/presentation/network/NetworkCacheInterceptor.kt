package com.gallery.kakaogallery.presentation.network

import android.util.Log
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

// cache 설정
// https://jizard.tistory.com/282
// https://bb-library.tistory.com/266 // cache - control 설명도 존재
// https://developer.mozilla.org/ko/docs/Web/HTTP/Headers/Cache-Control
// 검색 결과는 5분간 캐시하여, 5분 이내 동일 키워드로 검색했을 때 네트워크 통신없이 결과를 보여줍니다.
class NetworkCacheInterceptor : Interceptor {
    companion object {
        private const val TAG = "NetworkCacheInterceptor"
    }

    private val cacheSec = 60 * 5 // 5초정도 까여서 작동하는거같은데 왜지

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder().cacheControl(
            CacheControl.Builder()
                .maxAge(cacheSec, TimeUnit.SECONDS)
                //클라이언트가 캐시의 만료 시간을 초과한 응답을 받아들일지를 나타냅니다. 부가적으로, 초 단위의 값을 할당할 수 있는데, 이는 응답이 결코 만료되서는 안되는 시간을 나타냅니다.
                .maxStale(cacheSec, TimeUnit.SECONDS) // cacheSec 동안 절대 만료되지않는다
                .build()
        ).build()

        val response = chain.proceed(request)
        if (response.networkResponse != null) {
            Log.d(TAG, "is from network")
        } else if (response.cacheResponse != null) {
            Log.d(TAG, "is from cache")
        }
        return response
    }
}