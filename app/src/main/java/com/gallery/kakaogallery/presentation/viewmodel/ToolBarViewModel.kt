package com.gallery.kakaogallery.presentation.viewmodel

import kotlinx.coroutines.flow.StateFlow

interface ToolBarViewModel {
    val headerTitle: StateFlow<String>
}