package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gallery.kakaogallery.domain.model.GalleryImageListTypeModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.usecase.FetchSaveImageUseCase
import com.gallery.kakaogallery.domain.usecase.RemoveSaveImageUseCase
import com.gallery.kakaogallery.presentation.application.StringResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
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

    private val _saveImages: MutableLiveData<List<GalleryImageListTypeModel>> = handle.getLiveData(KEY_SAVE_IMAGE_LIST, emptyList())
    val saveImages: LiveData<List<GalleryImageListTypeModel>> = _saveImages

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

    init {
        bindAction()
        uiAction.onNext(UiAction.FetchSaveImages)
    }

    private fun bindAction() {
        uiAction.filter { action -> action is UiAction.Refresh }
            .subscribe {
                _refreshLoading.value = true
            }

        uiAction
            .filter { it is UiAction.FetchSaveImages }
            .flatMap {
                _dataLoading.value = true
                fetchSaveImageUseCase()
                    .takeUntil(uiAction.filter { action -> action is UiAction.Refresh })
                    .doFinally { Timber.d("dispose debug => dispose fetchSaveImageUseCase Stream") }
            }
            .doOnNext { Timber.d("dispose debug => emit data from fetchAction") }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                processFetchSaveImages(it)
            }.addTo(compositeDisposable)

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

        uiAction
            .filter { it is UiAction.ClickImageNoneSelectModeEvent }
            .cast(UiAction.ClickImageNoneSelectModeEvent::class.java)
            .throttleFirst(500, TimeUnit.MILLISECONDS)
            .subscribe {
                viewModelScope.launch { _uiEvent.emit(UiEvent.NavigateImageDetail(it.imageUrl, it.position)) }
            }.addTo(compositeDisposable)
    }

    private fun processFetchSaveImages(res: Result<List<GalleryImageListTypeModel>>){
        _dataLoading.value = false
        _refreshLoading.value = false
        res.onSuccess {
            setNotifyGroup(
                it.isEmpty(),
                resourceProvider.getString(StringResourceProvider.StringResourceId.EmptySaveImage),
                emptyNotifyBtn
            )
            _saveImages.value = it
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
                showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.RemoveSuccess))
                clickSelectModeEvent() // 삭제라면 선택모드 였을테니, toggle 해주면 선택모드가 해제됨
            }
            else -> showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.RemoveFail))
        }
    }

    private fun processRemoveSelectImageException(throwable: Throwable){
        _dataLoading.value = false
        throwable.printStackTrace()
        showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.RemoveFail) + " $throwable")
    }

    fun clickNotifyEvent() {
        when(notifyBtn.value) {
            emptyNotifyBtn -> viewModelScope.launch { _uiEvent.emit(UiEvent.NavigateSearchView) }
            retryNotifyBtn -> refreshGalleryEvent()
        }
    }

    fun refreshGalleryEvent() {
        uiAction.onNext(UiAction.Refresh)
        uiAction.onNext(UiAction.FetchSaveImages)
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
            _uiEvent.emit(UiEvent.ScrollToTop((saveImages.value?.size ?: 0) <= 80))
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
            else ->
                uiAction.onNext(UiAction.ClickImageNoneSelectModeEvent(image.imageUrl, idx))
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
            handle[KEY_HEADER_TITLE] = resourceProvider.getString(
                StringResourceProvider.StringResourceId.SelectState,
                selectImageHashMap.size
            )
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SelectFail))
        }
    }

    private fun showToast(message: String) {
        viewModelScope.launch { _uiEvent.emit(UiEvent.ShowToast(message)) }
    }

    private fun showSnackBar(message: String, action: Pair<String, () -> Unit>?) {
        viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackBar(message, action)) }
    }

    private fun setNotifyGroup(visible: Boolean, message: String, btn: String) {
        handle[KEY_NOTIFY_GROUP_VISIBLE] = visible
        handle[KEY_NOTIFY_TEXT] = message
        handle[KEY_NOTIFY_BTN] = btn
    }

    sealed class UiAction {
        object FetchSaveImages : UiAction()
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