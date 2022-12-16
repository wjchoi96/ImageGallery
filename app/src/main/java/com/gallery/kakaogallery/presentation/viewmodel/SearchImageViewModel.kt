package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.gallery.kakaogallery.domain.model.SearchImageListTypeModel
import com.gallery.kakaogallery.domain.usecase.FetchQueryDataUseCase
import com.gallery.kakaogallery.domain.usecase.SaveSelectImageUseCase
import com.gallery.kakaogallery.presentation.application.StringResourceProvider
import com.gallery.kakaogallery.presentation.extension.throttleFirst
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.Serializable
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class SearchImageViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val resourceProvider: StringResourceProvider,
    private val fetchSearchDataQueryDataUseCase: FetchQueryDataUseCase,
    private val saveSelectImageUseCase: SaveSelectImageUseCase
) : DisposableManageViewModel(), ToolBarViewModel {
    companion object {
        private const val KEY_SEARCH_QUERY_INFO = "key_search_query_info"
        private const val KEY_SELECT_IMAGE_MAP = "key_select_image_map"
        private const val KEY_SELECT_MODE = "key_select_mode"
        private const val KEY_HEADER_TITLE = "key_header_title"
        private const val KEY_NOTIFY_TEXT = "key_notify_text"
    }

    private val searchFailMessage: String =
        resourceProvider.getString(StringResourceProvider.StringResourceId.SearchFail)
    private val selectImageUrlMap: MutableMap<String, Int> = handle[KEY_SELECT_IMAGE_MAP] ?: kotlin.run {
        mutableMapOf<String, Int>().also { handle[KEY_SELECT_IMAGE_MAP] = it }
    }

    private val _searchImages: MutableStateFlow<List<SearchImageListTypeModel>> = MutableStateFlow(emptyList())
    val searchImages = _searchImages.asStateFlow()

    override val headerTitle: StateFlow<String> = handle.getStateFlow(KEY_HEADER_TITLE, resourceProvider.getString(StringResourceProvider.StringResourceId.MenuSearchImage))

    val selectMode: StateFlow<Boolean> = handle.getStateFlow(KEY_SELECT_MODE, false)

    val notifyText: StateFlow<String> = handle.getStateFlow(KEY_NOTIFY_TEXT, "")

    private val _searchResultIsEmpty = MutableStateFlow(false)
    val searchResultIsEmpty = _searchResultIsEmpty.asStateFlow()

    private val _dataLoading = MutableStateFlow(false)
    val dataLoading = _dataLoading.asStateFlow()

    private val _pagingDataLoading = MutableStateFlow(false)
    val pagingDataLoading = _pagingDataLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private val searchInfo: StateFlow<SearchInfo> = handle.getStateFlow(KEY_SEARCH_QUERY_INFO, SearchInfo("", 1))
    private val uiAction: PublishSubject<UiAction> = PublishSubject.create()
    private val uiActionFlow = MutableSharedFlow<UiAction>()


    init {
        bindAction()
    }

    @FlowPreview
    private fun bindAction(){
        viewModelScope.launch {
            launch {
                uiActionFlow
                    .filterIsInstance<UiAction.SearchAction>()
                    .filter { it.page == 1 }
                    .flatMapConcat {
                        _uiEvent.emit(UiEvent.KeyboardVisibleEvent(false))
                        when (dataLoading.value) {
                            true -> flow {
                                emit(Result.failure(Throwable(resourceProvider.getString(StringResourceProvider.StringResourceId.Loading))))
                            }
                            else -> {
                                if(it.query != searchInfo.value.query && selectImageUrlMap.isNotEmpty()) {
                                    selectImageUrlMap.clear()
                                    setHeaderTitleUseSelectMap()
                                }
                                _dataLoading.value = true
                                fetchSearchDataQueryDataUseCase(it.query, it.page)
                            }
                        }
                    }
                    .flowOn(Dispatchers.Default)
                    .collect {
                        Timber.d("collect run at ${Thread.currentThread().name}")
                        Timber.d("search query subscribe => $it")
                        processSearchResult(it)
                    }
            }

            launch {
                uiActionFlow
                    .filterIsInstance<UiAction.SearchAction>()
                    .filter { it.page > 1 }
                    .filter { it.query.isNotBlank() && !pagingDataLoading.value }
                    .flatMapConcat {
                        _pagingDataLoading.value = true
                        fetchSearchDataQueryDataUseCase(it.query, it.page)
                    }.flowOn(Dispatchers.Default)
                    .collect {
                        processPagingResult(it)
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
                searchInfo
                    .collect {
                        Timber.d("searchInfo emit $it")
                        uiActionFlow.emit(UiAction.SearchAction(it.query, it.page))
                    }
            }

        }

        uiAction
            .filter { it is UiAction.SaveSelectImage }
            .cast(UiAction.SaveSelectImage::class.java)
            .flatMapSingle {
                _dataLoading.value = true
                when (it.selectImageMap.isEmpty() || it.images == null) {
                    true ->
                        Single.error(Throwable(resourceProvider.getString(StringResourceProvider.StringResourceId.NoneSelectImage)))
                    else -> saveSelectImageUseCase(
                        it.selectImageMap,
                        it.images
                    )
                }
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                processSaveImageResult(it)
            }){
                processSaveImageException(it)
            }.addTo(compositeDisposable)

    }

    private suspend fun processSearchResult(res: Result<List<SearchImageListTypeModel>>){
        _dataLoading.value = false
        res.onSuccess {
            if (searchInfo.value.query.isNotBlank()) _searchResultIsEmpty.value = it.size <= 1
            when (selectImageUrlMap.isEmpty()) {
                true -> {
                    handle[KEY_NOTIFY_TEXT] = resourceProvider.getString(StringResourceProvider.StringResourceId.EmptySearchResult)
                    _searchImages.emit(it)
                }
                else -> {
                    _searchImages.emit(
                        it.map { item ->
                            when {
                                item is SearchImageListTypeModel.Image &&
                                    selectImageUrlMap.containsKey(item.image.imageUrl) -> item.copy(image = item.image.copy(isSelect = true))
                                else -> item
                            }
                        }
                    )
                    selectImageUrlMap.entries.removeIf { entry -> entry.value >= it.size }
                    setHeaderTitleUseSelectMap()
                }
            }
        }.onFailure {
            when (it) {
                is MaxPageException -> showSnackBar(
                    resourceProvider.getString(
                        StringResourceProvider.StringResourceId.LastPage
                    ), null
                )
                else -> {
                    "$searchFailMessage\n${it.message}".let { msg ->
                        showSnackBar(
                            msg,
                            resourceProvider.getString(
                                StringResourceProvider.StringResourceId.Retry
                            ) to {
                                searchInfo.value.let {
                                    viewModelScope.launch { uiActionFlow.emit(UiAction.SearchAction(it. query, it.page)) }
                                }
                            }
                        )
                        handle[KEY_NOTIFY_TEXT] = msg
                    }
                }
            }
        }
    }

    private suspend fun processPagingResult(res: Result<List<SearchImageListTypeModel>>) {
        _pagingDataLoading.value = false
        res.onSuccess {
            _searchImages.emit(searchImages.value + it)
        }.onFailure {
            when (it) {
                is MaxPageException -> showSnackBar(
                    resourceProvider.getString(
                        StringResourceProvider.StringResourceId.LastPage
                    ), null
                )
                else -> showSnackBar("$searchFailMessage\n${it.message}", null)
            }
        }
    }

    private fun processSaveImageResult(res: Boolean) {
        _dataLoading.value = false
        when (res) {
            true -> {
                viewModelScope.launch {
                    showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SaveSuccess))
                }
                clickSelectModeEvent()
            }
            else -> viewModelScope.launch {
                showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SaveFail))
            }
        }
    }

    private fun processSaveImageException(throwable: Throwable) {
        _dataLoading.value = false
        throwable.printStackTrace()
        viewModelScope.launch {
            showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SaveFail) + " $throwable")
        }
    }

    fun saveSelectImage() {
        uiAction.onNext(UiAction.SaveSelectImage(
            selectImageUrlMap,
            searchImages.value
        ))
    }

    fun backgroundTouchEvent() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.KeyboardVisibleEvent(false))
        }
    }

    fun clickSaveEvent() {
        when (selectImageUrlMap.size) {
            0 -> viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast(resourceProvider.getString(StringResourceProvider.StringResourceId.NoneSelectImage)))
            }
            else -> viewModelScope.launch {
                _uiEvent.emit(UiEvent.PresentSaveDialog(selectImageUrlMap.size))
            }
        }
    }

    fun clickSelectModeEvent() {
        when (selectMode.value) {
            true -> unSelectAllImage()
            else -> {}
        }
        setHeaderTitleUseSelectMap()
        handle[KEY_SELECT_MODE] = !selectMode.value
    }

    private fun setHeaderTitleUseSelectMap() {
        when (selectImageUrlMap.isEmpty()) {
            true -> handle[KEY_HEADER_TITLE] =
                resourceProvider.getString(StringResourceProvider.StringResourceId.MenuSearchImage)
            else -> handle[KEY_HEADER_TITLE] = resourceProvider.getString(
                StringResourceProvider.StringResourceId.SelectState,
                selectImageUrlMap.size
            )
        }
    }

    private fun unSelectAllImage() {
        val images = searchImages.value.toMutableList()
        try {
            for (idx in selectImageUrlMap.values) {
                images[idx] = (images[idx] as SearchImageListTypeModel.Image).let {
                    it.copy(image = it.image.copy(isSelect = false))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        selectImageUrlMap.clear()
        viewModelScope.launch { _searchImages.emit(images) }
    }

    private fun setSelectImage(image: ImageModel, idx: Int, select: Boolean) {
        val images = searchImages.value.toMutableList()
        try {
            images[idx] = (images[idx] as SearchImageListTypeModel.Image).let {
                it.copy(image = it.image.copy(isSelect = select))
            }
            when (select) {
                true -> selectImageUrlMap[image.imageUrl] = idx
                else -> selectImageUrlMap.remove(image.imageUrl)
            }
            viewModelScope.launch { _searchImages.emit(images) }
            setHeaderTitleUseSelectMap()
        } catch (e: Exception) {
            e.printStackTrace()
            viewModelScope.launch {
                showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SelectFail))
            }
        }
    }


    fun touchImageEvent(image: ImageModel, idx: Int) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.KeyboardVisibleEvent(false))
        }
        when (selectMode.value) {
            true ->
                setSelectImage(image, idx, !selectImageUrlMap.containsKey(image.imageUrl))
            else -> viewModelScope.launch {
                uiActionFlow.emit(UiAction.ClickImageNoneSelectModeEvent(image.imageUrl, idx))
            }
        }
    }

    fun searchQueryEvent(query: String) {
        viewModelScope.launch {
            when (searchInfo.value == SearchInfo(query, 1)) {
                true -> uiActionFlow.emit(UiAction.SearchAction(query, 1))
                else -> handle[KEY_SEARCH_QUERY_INFO] = SearchInfo(query, 1)
            }
        }
    }

    fun fetchNextPage() {
        viewModelScope.launch {
            searchInfo.value.let {
                handle[KEY_SEARCH_QUERY_INFO] = SearchInfo(it.query, it.page + 1)
            }
        }
    }

    fun touchToolBarEvent() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ScrollToTop(searchInfo.value.page == 1))
        }
    }

    private suspend fun showToast(message: String) {
        _uiEvent.emit(UiEvent.ShowToast(message))
    }

    private suspend fun showSnackBar(message: String, action: Pair<String, () -> Unit>?) {
        _uiEvent.emit(UiEvent.ShowSnackBar(message, action))
    }

    private data class SearchInfo(
        val query: String,
        val page: Int
    ) : Serializable

    sealed class UiAction {
        data class SearchAction(
            val query: String,
            val page: Int
        ) : UiAction()
        data class SaveSelectImage(
            val selectImageMap: MutableMap<String, Int>,
            val images: List<SearchImageListTypeModel>?
        ) : UiAction()
        data class ClickImageNoneSelectModeEvent(val imageUrl: String, val position: Int) : UiAction()
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data class ShowSnackBar(val message: String, val action: (Pair<String, ()->Unit>)?) : UiEvent()
        data class PresentSaveDialog(val selectCount: Int) : UiEvent()
        data class KeyboardVisibleEvent(val visible: Boolean) : UiEvent()
        data class ScrollToTop(val smoothScroll: Boolean) : UiEvent()
        data class NavigateImageDetail(val imageUrl: String, val position: Int) : UiEvent()
    }
}
