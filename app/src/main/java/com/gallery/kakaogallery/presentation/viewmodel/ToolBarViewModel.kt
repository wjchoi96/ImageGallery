package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.LiveData

interface ToolBarViewModel {
    val headerTitle: LiveData<String>
}