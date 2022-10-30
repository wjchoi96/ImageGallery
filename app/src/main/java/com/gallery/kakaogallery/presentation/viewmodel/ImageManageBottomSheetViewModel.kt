package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber

class ImageManageBottomSheetViewModel : ViewModel() {
    private val _content = MutableLiveData<String>()
    val content: LiveData<String> = _content

    private val _positiveBtnText = MutableLiveData<String>()
    val positiveBtnText: LiveData<String> = _positiveBtnText
    private val _negativeBtnText = MutableLiveData<String>()
    val negativeBtnText: LiveData<String> = _negativeBtnText

    private val _positiveBtnVisible = MutableLiveData(false)
    val positiveBtnVisible: LiveData<Boolean> = _positiveBtnVisible
    private val _negativeBtnVisible = MutableLiveData(false)
    val negativeBtnVisible: LiveData<Boolean> = _negativeBtnVisible

    private val _uiEvent = MutableLiveData<SingleEvent<UiEvent>>()
    val uiEvent: LiveData<SingleEvent<UiEvent>> = _uiEvent

    private var positiveListener: (() -> Unit)? = null
    private var negativeListener: (() -> Unit)? = null

    fun initData(
        message: String?,
        positiveBtnText: String,
        negativeBtnText: String?,
        positiveListener: (() -> Unit)? = null,
        negativeListener: (() -> Unit)? = null
    ){
        Timber.d("initData at bottom sheet => $message")
        if(message.isNullOrBlank()){
            _uiEvent.value = SingleEvent(UiEvent.Dismiss)
            return
        }
        message.let { _content.value = it }

        _positiveBtnText.value = positiveBtnText
        _positiveBtnVisible.value = true

        negativeBtnText?.let { _negativeBtnText.value = it }
        _negativeBtnVisible.value = negativeBtnText != null

        positiveListener?.let { this.positiveListener = it }
        negativeListener?.let { this.negativeListener = it }
    }

    fun positiveEvent(){
        positiveListener?.invoke()
        _uiEvent.value = SingleEvent(UiEvent.Dismiss)
    }

    fun negativeEvent(){
        negativeListener?.invoke()
        _uiEvent.value = SingleEvent(UiEvent.Dismiss)
    }

    sealed class UiEvent {
        object Dismiss: UiEvent()
    }
}