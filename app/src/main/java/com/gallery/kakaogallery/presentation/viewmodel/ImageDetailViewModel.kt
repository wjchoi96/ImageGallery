package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImageDetailViewModel @Inject constructor(
    private val handle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_IMAGE_URL = "key_image_url"
        const val EXTRA_IMAGE_DETAIL = "extra_image_detail"
    }

    /**
     * loadInfo.first => url
     * loadInfo.second => cache
     * 이미지가 로딩이 늦어질 경우를 대비해, 캐시에서 이미지를 우선 검색
     */
    private val _imageUrl: MutableLiveData<String> = handle.getLiveData(KEY_IMAGE_URL)
    val imageUrl: LiveData<String> = _imageUrl

    private val _loadOnlyCache = MutableLiveData(true)
    val loadOnlyCache: LiveData<Boolean> = _loadOnlyCache

    private val _uiEvent: MutableLiveData<SingleEvent<UiEvent>> = MutableLiveData()
    val uiEvent: LiveData<SingleEvent<UiEvent>> = _uiEvent

    private var prevLoadSuccess = false

    init {
        for(key in handle.keys()){
            when (key) {
                EXTRA_IMAGE_DETAIL -> { // 캐시에서 썸네일을 우선 load
                    when (handle.get<String>(key)) {
                        null -> _uiEvent.value = SingleEvent(UiEvent.Dismiss)
                        else -> _imageUrl.value = handle[key]
                    }
                }
            }
        }
    }

    fun reloadIfCacheLoadFail() {
        if(loadOnlyCache.value == true && !prevLoadSuccess) {
            _loadOnlyCache.value = false
        }
    }

    val finishLoadImageEvent: (Boolean) -> Unit = {
        Timber.d("animation debug => finishLoadImageEvent[$it]")
        prevLoadSuccess = it
        _uiEvent.value = SingleEvent(UiEvent.PostLoadImage)
    }

    sealed class UiEvent {
        object PostLoadImage : UiEvent()
        object Dismiss : UiEvent()
    }
}