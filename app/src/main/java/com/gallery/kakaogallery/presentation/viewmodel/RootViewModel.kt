package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class RootViewModel @Inject constructor(
    private val handle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val KEY_CURRENT_PAGE = "key_current_page"
    }
    val currentPage: StateFlow<Int> = handle.getStateFlow(KEY_CURRENT_PAGE, 0)

    fun clickTabEvent(idx: Int): Boolean{
        require(idx >= 0)
        handle[KEY_CURRENT_PAGE] = idx
        return true
    }

}