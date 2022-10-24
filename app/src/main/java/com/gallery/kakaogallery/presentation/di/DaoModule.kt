package com.gallery.kakaogallery.presentation.di

import com.gallery.kakaogallery.data.dao.SaveImageDao
import com.gallery.kakaogallery.data.dao.SaveImageDaoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DaoModule {

    @Binds
    @Singleton
    abstract fun bindSaveImageDao(impl: SaveImageDaoImpl): SaveImageDao
}