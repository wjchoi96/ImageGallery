package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class RootViewModel @Inject constructor(
    handle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val KEY_CURRENT_PAGE = "key_current_page"
    }
    private val _currentPage: MutableLiveData<Int> = handle.getLiveData(KEY_CURRENT_PAGE, 0)
    val currentPage: LiveData<Int> = _currentPage

    fun clickTabEvent(idx: Int): Boolean{
        require(idx >= 0)
        _currentPage.value = idx
        return true
    }

}