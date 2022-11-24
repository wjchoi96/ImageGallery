package com.gallery.kakaogallery.presentation.di

import com.gallery.kakaogallery.data.datasource.ImageSearchDataSource
import com.gallery.kakaogallery.data.datasource.SaveImageDataSource
import com.gallery.kakaogallery.data.datasource.VideoSearchDataSource
import com.gallery.kakaogallery.data.repository.ImageRepositoryImpl
import com.gallery.kakaogallery.domain.repository.ImageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideImageRepository(
        imageSearchDataSource: ImageSearchDataSource,
        videoSearchDataSource: VideoSearchDataSource,
        saveImageDataSource: SaveImageDataSource
    ): ImageRepository = ImageRepositoryImpl(
        imageSearchDataSource,
        videoSearchDataSource,
        saveImageDataSource
    )
}