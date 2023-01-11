package com.gallery.kakaogallery.presentation.di

import com.gallery.kakaogallery.data.repository.ImageRepositoryImpl
import com.gallery.kakaogallery.domain.repository.ImageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun provideImageRepository(impl: ImageRepositoryImpl): ImageRepository
}