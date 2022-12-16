package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gallery.kakaogallery.domain.model.GalleryImageListTypeModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.usecase.FetchSaveImageUseCase
import com.gallery.kakaogallery.domain.usecase.RemoveSaveImageUseCase
import com.gallery.kakaogallery.presentation.application.StringResourceProvider
import com.gallery.kakaogallery.presentation.extension.throttleFirst
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@FlowPreview
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
        private const val KEY_NOTIFY_GROUP_VISIBLE = "key_notify_text_visible"
        private const val KEY_NOTIFY_TEXT = "key_notify_text"
        private const val KEY_NOTIFY_BTN = "key_notify_btn"
    }

    private val fetchImageFailMessage: String =
        resourceProvider.getString(StringResourceProvider.StringResourceId.FetchFailSaveImage)
    private val emptyNotifyBtn = resourceProvider.getString(StringResourceProvider.StringResourceId.MenuSearchImage)
    private val retryNotifyBtn = resourceProvider.getString(StringResourceProvider.StringResourceId.Retry)

    private val selectImageHashMap: MutableMap<String, Int> = handle[KEY_SELECT_IMAGE_MAP] ?: kotlin.run {
        mutableMapOf<String, Int>().also { handle[KEY_SELECT_IMAGE_MAP] = it }
    }

    val saveImages: StateFlow<List<GalleryImageListTypeModel>> = handle.getStateFlow(KEY_SAVE_IMAGE_LIST, emptyList())

    override val headerTitle: StateFlow<String> = handle.getStateFlow(KEY_HEADER_TITLE, resourceProvider.getString(StringResourceProvider.StringResourceId.MenuGallery))

    val selectMode: StateFlow<Boolean> = handle.getStateFlow(KEY_SELECT_MODE, false)

    val notifyGroupVisible: StateFlow<Boolean> = handle.getStateFlow(KEY_NOTIFY_GROUP_VISIBLE, false)

    val notifyText: StateFlow<String> = handle.getStateFlow(KEY_NOTIFY_TEXT, "")

    val notifyBtn: StateFlow<String> = handle.getStateFlow(KEY_NOTIFY_BTN, "")

    private val _dataLoading = MutableStateFlow(false)
    val dataLoading: StateFlow<Boolean> = _dataLoading.asStateFlow()

    private val _refreshLoading = MutableStateFlow(false)
    val refreshLoading: StateFlow<Boolean> = _refreshLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val uiAction: PublishSubject<UiAction> = PublishSubject.create()

    private val uiActionFlow = MutableSharedFlow<UiAction>()
    private var saveImageStreamJob: Job? = null

    init {
        bindAction()
        fetchSaveImagesStream()
    }

    private fun bindAction() {
        viewModelScope.launch {
            launch {
                uiActionFlow
                    .collect {
                        Timber.d("refresh debug => uiActionFlow collect $it")
                    }
            }

            launch {
                uiActionFlow
                    .filterIsInstance<UiAction.ClickImageNoneSelectModeEvent>()
                    .throttleFirst(500)
                    .collect {
                        _uiEvent.emit(UiEvent.NavigateImageDetail(it.imageUrl, it.position))
                    }
            }

            launch {
                uiActionFlow
                    .filterIsInstance<UiAction.Refresh>()
                    .collect {
                        Timber.d("refresh debug => Refresh on")
                        _refreshLoading.value = true
                        saveImageStreamJob?.cancel()
                        fetchSaveImagesStream()
                    }
            }
        }

        uiAction
            .filter { it is UiAction.RemoveSelectImage }
            .cast(UiAction.RemoveSelectImage::class.java)
            .flatMapSingle {
                _dataLoading.value = true
                removeSaveImageUseCase(it.selectImageMap)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                processRemoveSelectImage(it)
            }) {
                processRemoveSelectImageException(it)
            }.addTo(compositeDisposable)
    }

    private fun fetchSaveImagesStream() {
        viewModelScope.launch {
            saveImageStreamJob = launch {
                fetchSaveImageUseCase()
                    .flowOn(Dispatchers.Default)
                    .collect {
                        processFetchSaveImages(it)
                    }
            }
        }
    }

    private suspend fun processFetchSaveImages(res: Result<List<GalleryImageListTypeModel>>){
        _dataLoading.value = false
        _refreshLoading.value = false
        Timber.d("refresh debug => processFetchSaveImages [${_refreshLoading.value}]")
        res.onSuccess {
            setNotifyGroup(
                it.isEmpty(),
                resourceProvider.getString(StringResourceProvider.StringResourceId.EmptySaveImage),
                emptyNotifyBtn
            )
            handle[KEY_SAVE_IMAGE_LIST] = it
        }.onFailure {
            "$fetchImageFailMessage\n${it.message}".let { msg ->
                showSnackBar(
                    msg,
                    resourceProvider.getString(
                        StringResourceProvider.StringResourceId.Retry
                    ) to {
                        refreshGalleryEvent()
                    }
                )
                setNotifyGroup(true, msg, retryNotifyBtn)
            }
        }
    }

    private fun processRemoveSelectImage(res: Boolean) {
        _dataLoading.value = false
        when (res) {
            true -> {
                viewModelScope.launch {
                    showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.RemoveSuccess))
                }
                clickSelectModeEvent() // 삭제라면 선택모드 였을테니, toggle 해주면 선택모드가 해제됨
            }
            else -> viewModelScope.launch {
                showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.RemoveFail))
            }
        }
    }

    private fun processRemoveSelectImageException(throwable: Throwable){
        _dataLoading.value = false
        throwable.printStackTrace()
        viewModelScope.launch {
            showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.RemoveFail) + " $throwable")
        }
    }

    fun clickNotifyEvent() {
        when(notifyBtn.value) {
            emptyNotifyBtn -> viewModelScope.launch { _uiEvent.emit(UiEvent.NavigateSearchView) }
            retryNotifyBtn -> refreshGalleryEvent()
        }
    }

    fun refreshGalleryEvent() {
        viewModelScope.launch { uiActionFlow.emit(UiAction.Refresh) }
    }

    fun removeSelectImage() {
        uiAction.onNext(UiAction.RemoveSelectImage(selectImageHashMap))
    }

    fun clickRemoveEvent() {
        if (selectImageHashMap.isEmpty()) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast(resourceProvider.getString(StringResourceProvider.StringResourceId.NoneSelectImage)))
            }
            return
        }
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.PresentRemoveDialog(selectImageHashMap.size))
        }
    }

    fun touchToolBarEvent() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ScrollToTop(saveImages.value.size <= 80))
        }
    }

    fun clickSelectModeEvent() {
        when (selectMode.value) {
            true -> {
                unSelectAllImage()
                handle[KEY_HEADER_TITLE] = resourceProvider.getString(StringResourceProvider.StringResourceId.MenuGallery)
            }
            else -> handle[KEY_HEADER_TITLE] = resourceProvider.getString(
                StringResourceProvider.StringResourceId.SelectState,
                selectImageHashMap.size
            )
        }
        handle[KEY_SELECT_MODE] = !selectMode.value
    }

    fun touchImageEvent(image: ImageModel, idx: Int) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.KeyboardVisibleEvent(false))
        }
        when (selectMode.value) {
            true ->
                setSelectImage(image, idx, !selectImageHashMap.containsKey(image.hash))
            else -> viewModelScope.launch {
                uiActionFlow.emit(UiAction.ClickImageNoneSelectModeEvent(image.imageUrl, idx))
            }
        }
    }

    private fun unSelectAllImage() {
        val images = saveImages.value.toMutableList()
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
        handle[KEY_SAVE_IMAGE_LIST] = images
    }

    private fun setSelectImage(image: ImageModel, idx: Int, select: Boolean) {
        val images = saveImages.value.toMutableList()
        try {
            images[idx] = (images[idx] as GalleryImageListTypeModel.Image).let {
                it.copy(image = it.image.copy(isSelect = select))
            }
            when (select) {
                true -> selectImageHashMap[image.hash] = idx
                else -> selectImageHashMap.remove(image.hash)
            }
            handle[KEY_SAVE_IMAGE_LIST] = images
            handle[KEY_HEADER_TITLE] = resourceProvider.getString(
                StringResourceProvider.StringResourceId.SelectState,
                selectImageHashMap.size
            )
        } catch (e: Exception) {
            e.printStackTrace()
            viewModelScope.launch {
                showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SelectFail))
            }
        }
    }

    private suspend fun showToast(message: String) {
        _uiEvent.emit(UiEvent.ShowToast(message))
    }

    private suspend fun showSnackBar(message: String, action: Pair<String, () -> Unit>?) {
        _uiEvent.emit(UiEvent.ShowSnackBar(message, action))
    }

    private fun setNotifyGroup(visible: Boolean, message: String, btn: String) {
        handle[KEY_NOTIFY_GROUP_VISIBLE] = visible
        handle[KEY_NOTIFY_TEXT] = message
        handle[KEY_NOTIFY_BTN] = btn
    }

    sealed class UiAction {
        object Refresh : UiAction()
        data class RemoveSelectImage(val selectImageMap: MutableMap<String, Int>) : UiAction()
        data class ClickImageNoneSelectModeEvent(val imageUrl: String, val position: Int) : UiAction()
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data class ShowSnackBar(val message: String, val action: (Pair<String, ()->Unit>)?) : UiEvent()
        data class PresentRemoveDialog(val selectCount: Int) : UiEvent()
        data class KeyboardVisibleEvent(val visible: Boolean) : UiEvent()
        data class ScrollToTop(val smoothScroll: Boolean) : UiEvent()
        object NavigateSearchView : UiEvent()
        data class NavigateImageDetail(val imageUrl: String, val position: Int) : UiEvent()
    }
}