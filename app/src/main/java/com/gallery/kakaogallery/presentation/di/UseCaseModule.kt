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
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideFetchQueryDataUseCase(
        imageRepository: ImageRepository,
        @DefaultDispatcher dispatcher: CoroutineDispatcher
    ) = FetchQueryDataUseCase(imageRepository, dispatcher)

    @Provides
    fun provideFetchSaveImageUseCase(
        imageRepository: ImageRepository,
        @DefaultDispatcher dispatcher: CoroutineDispatcher
    ) = FetchSaveImageUseCase(imageRepository, dispatcher)

    @Provides
    fun provideSaveSelectImageUseCase(
        imageRepository: ImageRepository,
        @DefaultDispatcher dispatcher: CoroutineDispatcher
    ) = SaveSelectImageUseCase(imageRepository, dispatcher)

    @Provides
    fun provideRemoveSaveImageUseCase(
        imageRepository: ImageRepository,
        @DefaultDispatcher dispatcher: CoroutineDispatcher
    ) = RemoveSaveImageUseCase(imageRepository, dispatcher)
    
}