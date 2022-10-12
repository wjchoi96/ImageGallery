package com.gallery.kakaogallery.presentation.di

import com.gallery.kakaogallery.data.datasource.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindImageSearchDataSource(impl: ImageSearchDataSourceImpl): ImageSearchDataSource

    @Binds
    abstract fun bindVideoSearchDataSource(impl: VideoSearchDataSourceImpl): VideoSearchDataSource

    @Binds
    abstract fun bindSaveImageDataSource(impl: SaveImageDataSourceImpl): SaveImageDataSource
}