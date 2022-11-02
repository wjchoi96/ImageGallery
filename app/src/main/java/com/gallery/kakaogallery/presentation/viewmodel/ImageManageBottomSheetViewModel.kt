package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.gallery.kakaogallery.presentation.application.StringResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageManageBottomSheetViewModel @Inject constructor(
    handle: SavedStateHandle,
    stringResourceProvider: StringResourceProvider
) : ViewModel() {
    companion object {
        const val KEY_CONTENT = "key_content"
        const val KEY_POSITIVE_TEXT = "key_positive_text"
        const val KEY_NEGATIVE_TEXT = "key_negative_text"
    }
    private val _content = MutableLiveData<String>()
    val content: LiveData<String> = _content

    private val _positiveBtnText = MutableLiveData<String>()
    val positiveBtnText: LiveData<String> = _positiveBtnText
    private val _negativeBtnText = MutableLiveData<String>()
    val negativeBtnText: LiveData<String> = _negativeBtnText

    private val _positiveBtnVisible = MutableLiveData(true)
    val positiveBtnVisible: LiveData<Boolean> = _positiveBtnVisible
    private val _negativeBtnVisible = MutableLiveData(false)
    val negativeBtnVisible: LiveData<Boolean> = _negativeBtnVisible

    private val _uiEvent = MutableLiveData<SingleEvent<UiEvent>>()
    val uiEvent: LiveData<SingleEvent<UiEvent>> = _uiEvent

    init {
        for(key in handle.keys()){
            when (key) {
                KEY_CONTENT -> _content.value = handle[key]
                KEY_POSITIVE_TEXT -> _positiveBtnText.value =
                    handle[key] ?: stringResourceProvider.getString(StringResourceProvider.StringResourceId.Confirm)
                KEY_NEGATIVE_TEXT -> _negativeBtnText.value = handle[key]
            }
        }
        if(_content.value.isNullOrBlank()){
            _uiEvent.value = SingleEvent(UiEvent.Dismiss)
        }
        _negativeBtnVisible.value = _negativeBtnText.value != null
    }

    fun positiveEvent(){
        _uiEvent.value = SingleEvent(UiEvent.PositiveEvent)
        _uiEvent.value = SingleEvent(UiEvent.Dismiss)
    }

    fun negativeEvent(){
        _uiEvent.value = SingleEvent(UiEvent.NegativeEvent)
        _uiEvent.value = SingleEvent(UiEvent.Dismiss)
    }

    sealed class UiEvent {
        object PositiveEvent: UiEvent()
        object NegativeEvent: UiEvent()
        object Dismiss: UiEvent()
    }
}