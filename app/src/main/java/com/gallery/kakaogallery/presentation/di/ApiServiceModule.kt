package com.gallery.kakaogallery.presentation.di

import com.gallery.kakaogallery.data.service.ImageSearchService
import com.gallery.kakaogallery.data.service.VideoSearchService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiServiceModule {

    @Provides
    @Singleton
    fun provideImageSearchApiService(
        retrofit: Retrofit
    ): ImageSearchService = retrofit.create(ImageSearchService::class.java)

    @Provides
    @Singleton
    fun provideVideoSearchApiService(
        retrofit: Retrofit
    ): VideoSearchService = retrofit.create(VideoSearchService::class.java)
}