package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.gallery.kakaogallery.domain.model.GalleryImageListTypeModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.gallery.kakaogallery.domain.usecase.FetchSaveImageUseCase
import com.gallery.kakaogallery.domain.usecase.RemoveSaveImageUseCase
import com.gallery.kakaogallery.presentation.application.StringResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val resourceProvider: StringResourceProvider,
    private val fetchSaveImageUseCase: FetchSaveImageUseCase,
    private val removeSaveImageUseCase: RemoveSaveImageUseCase
) : DisposableManageViewModel(), ToolBarViewModel {
    companion object {
        private const val KEY_SELECT_IMAGE_MAP = "key_select_image_map"
        private const val KEY_SAVE_IMAGE_LIST = "key_save_image_list"
        private const val KEY_SELECT_MODE = "key_select_mode"
        private const val KEY_HEADER_TITLE = "key_header_title"
    }

    private val fetchImageFailMessage: String =
        resourceProvider.getString(StringResourceProvider.StringResourceId.FetchFailSaveImage)
    private val selectImageHashMap: MutableMap<String, Int> = handle[KEY_SELECT_IMAGE_MAP] ?: kotlin.run {
        mutableMapOf<String, Int>().also { handle[KEY_SELECT_IMAGE_MAP] = it }
    }

    private val _saveImages: MutableLiveData<List<GalleryImageListTypeModel>> = handle.getLiveData(KEY_SAVE_IMAGE_LIST, emptyList())
    val saveImages: LiveData<List<GalleryImageListTypeModel>> = _saveImages

    private val _headerTitle: MutableLiveData<String> =
        handle.getLiveData(KEY_HEADER_TITLE, resourceProvider.getString(StringResourceProvider.StringResourceId.MenuGallery))
    override val headerTitle: LiveData<String> = _headerTitle

    private val _selectMode: MutableLiveData<Boolean> = handle.getLiveData(KEY_SELECT_MODE, false)
    val selectMode: LiveData<Boolean> = _selectMode

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _refreshLoading = MutableLiveData(false)
    val refreshLoading: LiveData<Boolean> = _refreshLoading

    private val _uiEvent = MutableLiveData<SingleEvent<UiEvent>>()
    val uiEvent: LiveData<SingleEvent<UiEvent>> = _uiEvent

    init {
        fetchSaveImages() // stream 연결을 위해 무조건 호출
        Timber.d("save state handle debug => ${selectImageHashMap.size}")
    }

    fun removeSelectImage() {
        _dataLoading.value = true
        removeSaveImageUseCase(selectImageHashMap)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _dataLoading.value = false
                when (it) {
                    true -> {
                        showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.RemoveSuccess))
                        clickSelectModeEvent() // 삭제라면 선택모드 였을테니, toggle 해주면 선택모드가 해제됨
                    }
                    else -> showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.RemoveFail))
                }
            }) {
                _dataLoading.value = false
                it.printStackTrace()
                showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.RemoveFail) + " $it")
            }.addTo(compositeDisposable)
    }

    private var saveImageDisposable: Disposable? = null
    private fun fetchSaveImages(isRefresh: Boolean = false) {
        saveImageDisposable?.dispose()
        saveImageDisposable = null

        _dataLoading.value = true
        saveImageDisposable = fetchSaveImageUseCase()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { res ->
                _dataLoading.value = false
                if (isRefresh) _refreshLoading.value = false
                res.onSuccess {
                    _saveImages.value = it
                }.onFailure {
                    when (it) {
                        is MaxPageException -> showToast(
                            resourceProvider.getString(
                                StringResourceProvider.StringResourceId.LastPage
                            )
                        )
                        else -> showToast("$fetchImageFailMessage\n${it.message}")
                    }
                }
            }.addTo(compositeDisposable)
    }

    fun refreshGalleryEvent() {
        _refreshLoading.value = true
        fetchSaveImages(isRefresh = true)
    }

    fun clickRemoveEvent() {
        if (selectImageHashMap.isEmpty()) {
            _uiEvent.value =
                SingleEvent(UiEvent.ShowToast(resourceProvider.getString(StringResourceProvider.StringResourceId.NoneSelectImage)))
            return
        }
        _uiEvent.value = SingleEvent(UiEvent.PresentRemoveDialog(selectImageHashMap.size))
    }

    fun clickSelectModeEvent() {
        when (_selectMode.value) {
            true -> {
                unSelectAllImage()
                _headerTitle.value =
                    resourceProvider.getString(StringResourceProvider.StringResourceId.MenuGallery)
            }
            else -> _headerTitle.value = resourceProvider.getString(
                StringResourceProvider.StringResourceId.SelectState,
                selectImageHashMap.size
            )
        }
        _selectMode.value = !(_selectMode.value ?: false)
    }

    fun touchImageEvent(image: ImageModel, idx: Int) {
        _uiEvent.value = SingleEvent(UiEvent.KeyboardVisibleEvent(false))
        when (selectMode.value) {
            true ->
                setSelectImage(image, idx, !selectImageHashMap.containsKey(image.hash))
            else -> {}
        }
    }

    private fun unSelectAllImage() {
        val images = saveImages.value?.toMutableList() ?: return
        try {
            for (idx in selectImageHashMap.values) {
                images[idx] = (images[idx] as GalleryImageListTypeModel.Image).let {
                    it.copy(image = it.image.copy(isSelect = false))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        selectImageHashMap.clear()
        _saveImages.value = images
    }

    private fun setSelectImage(image: ImageModel, idx: Int, select: Boolean) {
        val images = saveImages.value?.toMutableList() ?: return
        try {
            images[idx] = (images[idx] as GalleryImageListTypeModel.Image).let {
                it.copy(image = it.image.copy(isSelect = select))
            }
            when (select) {
                true -> selectImageHashMap[image.hash] = idx
                else -> selectImageHashMap.remove(image.hash)
            }
            _saveImages.value = images
            _headerTitle.value = resourceProvider.getString(
                StringResourceProvider.StringResourceId.SelectState,
                selectImageHashMap.size
            )
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SelectFail))
        }
    }

    private fun showToast(message: String) {
        _uiEvent.value = SingleEvent(UiEvent.ShowToast(message))
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data class PresentRemoveDialog(val selectCount: Int) : UiEvent()
        data class KeyboardVisibleEvent(val visible: Boolean) : UiEvent()
    }
}