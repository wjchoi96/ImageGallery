package com.gallery.kakaogallery.presentation.di

import com.gallery.kakaogallery.domain.repository.ImageRepository
import com.gallery.kakaogallery.domain.usecase.FetchQueryDataUseCase
import com.gallery.kakaogallery.domain.usecase.FetchSaveImageUseCase
import com.gallery.kakaogallery.domain.usecase.RemoveSaveImageUseCase
import com.gallery.kakaogallery.domain.usecase.SaveSelectImageUseCase
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

    @Provides
    fun provideFetchSaveImageUseCase(
        imageRepository: ImageRepository
    ) = FetchSaveImageUseCase(imageRepository)

    @Provides
    fun provideSaveSelectImageUseCase(
        imageRepository: ImageRepository
    ) = SaveSelectImageUseCase(imageRepository)

    @Provides
    fun provideRemoveSaveImageUseCase(
        imageRepository: ImageRepository
    ) = RemoveSaveImageUseCase(imageRepository)
    
}