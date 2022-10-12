package com.gallery.kakaogallery.presentation.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class RootViewModel @Inject constructor(): BaseViewModel() {

    var currentPage : Int = 0
}