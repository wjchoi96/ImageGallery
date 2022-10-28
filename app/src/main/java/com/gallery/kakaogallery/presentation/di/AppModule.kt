package com.gallery.kakaogallery.presentation.di

import android.app.Application
import android.content.Context
import com.gallery.kakaogallery.KakaoGallerySharedPreferences
import com.gallery.kakaogallery.presentation.application.StringResourceProvider
import com.gallery.kakaogallery.presentation.application.StringResourceProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AppModule
 *
 * Retrofit Module
 *
 * ApiService Module, Dao Module
 *
 * DataSourceModule
 *
 * Repository Module
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @ApplicationContext
    @Provides
    @Singleton
    fun provideApplicationContext(application: Application) = application

    @Provides
    @Singleton
    fun provideKakaoGallerySharedPreferences(
        @ApplicationContext context: Context
    ): KakaoGallerySharedPreferences = KakaoGallerySharedPreferences(context)

    @Provides
    @Singleton
    fun provideStringResourceProvider(
        @ApplicationContext context: Context
    ): StringResourceProvider = StringResourceProviderImpl(context)
}