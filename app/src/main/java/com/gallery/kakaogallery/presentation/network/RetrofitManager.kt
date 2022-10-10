package com.gallery.kakaogallery.presentation.network

import android.util.Log
import com.gallery.kakaogallery.ApiAddressConstant
import com.gallery.kakaogallery.BuildConfig
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/*
    interceptor : https://developer88.tistory.com/67
    application interceptor => application 에서 okHttp 로 넘어갈때 intercept
    http interceptor => okHttp 에서 network 로 넘어갈때 intercept
 */

class RetrofitManager private constructor() {
    companion object {
        private val TAG = KakaoGalleryApplication.getTag(this::class.java)
        val instance = RetrofitManager()
    }
    private val baseUrl : String = ApiAddressConstant.kakaoBaseUrl
    private val KAKAO_REST_API_KEY = KakaoGalleryApplication.instance.getString(R.string.kakao_rest_api_key)

    private var mRetrofit: Retrofit? = null
    private var mOkHttpClient: OkHttpClient? = null

    init {
        initOkHttpClient()
        initRetrofit()
    }
    // cache 설정
    // https://jizard.tistory.com/282
    // https://bb-library.tistory.com/266 // cache - control 설명도 존재
    // https://developer.mozilla.org/ko/docs/Web/HTTP/Headers/Cache-Control
    // 검색 결과는 5분간 캐시하여, 5분 이내 동일 키워드로 검색했을 때 네트워크 통신없이 결과를 보여줍니다.
    private fun getCacheInterceptor() : Interceptor{
        val cacheSec = 60*5 // 5초정도 까여서 작동하는거같은데 왜지
        return Interceptor {
            var request = it.request()
            request = request.newBuilder().cacheControl(CacheControl.Builder()
                    .maxAge(cacheSec, TimeUnit.SECONDS)
                    //클라이언트가 캐시의 만료 시간을 초과한 응답을 받아들일지를 나타냅니다. 부가적으로, 초 단위의 값을 할당할 수 있는데, 이는 응답이 결코 만료되서는 안되는 시간을 나타냅니다.
                    .maxStale(cacheSec, TimeUnit.SECONDS) // cacheSec 동안 절대 만료되지않는다
                    .build()
                ).build()

            val response = it.proceed(request)
            if(response.networkResponse != null){
                Log.d(TAG, "is from network")
            } else if(response.cacheResponse != null){
                Log.d(TAG, "is from cache")
            }
            response
        }
    }
    private fun getHttpLogInterceptor() : Interceptor {
        return HttpLoggingInterceptor().apply {
            // log setting
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    private fun getKakaoRestApiHeader() : Interceptor{
        return Interceptor {
            val original = it.request()
            it.proceed(original.newBuilder().apply {
                addHeader("Authorization", "KakaoAK $KAKAO_REST_API_KEY") // set header
            }.build())
        } // header setting
    }

    private fun initOkHttpClient() {
        val cache = Cache(KakaoGalleryApplication.instance.cacheDir, (5 * 1024 * 1024).toLong()) // 5mb => 캐시 용량은 한번 더 생각해서 설정해보자
        mOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(getKakaoRestApiHeader())
            .cache(cache)
            .addNetworkInterceptor(getHttpLogInterceptor()) // Network Interceptor
            .addInterceptor(getCacheInterceptor()) // Application Interceptor
            .build()
    }

    private fun initRetrofit() {
        if (mOkHttpClient == null)
            initOkHttpClient()
        mRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(mOkHttpClient!!)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) // for rx
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getCustomRetrofit(baseUrl: String): Retrofit {
        if (mOkHttpClient == null)
            initOkHttpClient()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) // for rx
            .addConverterFactory(GsonConverterFactory.create())
            .client(mOkHttpClient!!)
            .build()
    }

    fun <T> getService(service: Class<T>): T {
        return mRetrofit!!.create(service)
    }

}