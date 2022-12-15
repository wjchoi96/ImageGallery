package com.gallery.kakaogallery.presentation.di

import android.content.Context
import com.gallery.kakaogallery.BuildConfig
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.data.constant.ApiAddressConstant
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication
import com.gallery.kakaogallery.presentation.network.NetworkCacheInterceptor
import com.gallery.kakaogallery.presentation.network.NetworkConnectionInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Singleton
annotation class HttpLogInterceptor

@Qualifier
@Singleton
annotation class KakaoApiHeaderInterceptor


@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    fun provideNetworkCacheInterceptor() = NetworkCacheInterceptor()

    @Provides
    fun provideNetworkConnectionInterceptor(
        @ApplicationContext context: Context
    ) = NetworkConnectionInterceptor(context)

    @Provides
    @HttpLogInterceptor
    fun provideHttpLogInterceptor() = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @KakaoApiHeaderInterceptor
    fun provideKakaoApiHeaderInterceptor(
        @ApplicationContext context: Context
    ) = Interceptor {
        val original = it.request()
        it.proceed(original.newBuilder().apply {
            addHeader("Authorization", "KakaoAK ${context.getString(R.string.kakao_rest_api_key)}") // set header
        }.build())
    }

    @Provides
    fun provideOkHttpClient(
        networkCacheInterceptor: NetworkCacheInterceptor,
        networkConnectionInterceptor: NetworkConnectionInterceptor,
        @HttpLogInterceptor httpLogInterceptor: HttpLoggingInterceptor,
        @KakaoApiHeaderInterceptor kakaoApiHeaderInterceptor: Interceptor
    ): OkHttpClient {
        val cache = Cache(KakaoGalleryApplication.instance.cacheDir, (5 * 1024 * 1024).toLong()) // 5mb => 캐시 용량은 한번 더 생각해서 설정해보자
        return OkHttpClient.Builder()
            .addInterceptor(kakaoApiHeaderInterceptor)
            .cache(cache)
            .addNetworkInterceptor(httpLogInterceptor) // Network Interceptor
            .addInterceptor(networkCacheInterceptor) // Application Interceptor
            .addInterceptor(networkConnectionInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(ApiAddressConstant.kakaoBaseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


}