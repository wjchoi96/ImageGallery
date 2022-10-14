package com.gallery.kakaogallery.presentation.di

import com.gallery.kakaogallery.domain.repository.ImageRepository
import com.gallery.kakaogallery.domain.usecase.FetchQueryDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideFetchQueryDataUseCase(
        imageRepository: ImageRepository
    ) = FetchQueryDataUseCase(imageRepository)

    
}