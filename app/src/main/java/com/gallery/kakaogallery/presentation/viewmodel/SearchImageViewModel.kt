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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchImageViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val resourceProvider: StringResourceProvider,
    private val fetchSearchDataQueryDataUseCase: FetchQueryDataUseCase,
    private val saveSelectImageUseCase: SaveSelectImageUseCase
) : DisposableManageViewModel(), ToolBarViewModel {
    companion object {
        private const val KEY_SELECT_IMAGE_MAP = "key_select_image_map"
        private const val KEY_SEARCH_IMAGE_LIST = "key_search_image_list"
        private const val KEY_SELECT_MODE = "key_select_mode"
        private const val KEY_HEADER_TITLE = "key_header_title"
        private const val KEY_LAST_QUERY = "key_last_query"
        private const val KEY_CURRENT_PAGE = "key_current_page"
        private const val KEY_NOTIFY_TEXT = "key_notify_text"
    }
    private var currentPage: Int= handle[KEY_CURRENT_PAGE] ?: 1
        set(value) {
            handle[KEY_CURRENT_PAGE] = value
            field = value
        }

    private var lastQuery: String? = handle[KEY_LAST_QUERY]
        set(value) {
            handle[KEY_LAST_QUERY] = value
            field = value
        }

    private val searchFailMessage: String =
        resourceProvider.getString(StringResourceProvider.StringResourceId.SearchFail)
    private val selectImageUrlMap: MutableMap<String, Int> = handle[KEY_SELECT_IMAGE_MAP] ?: kotlin.run {
        mutableMapOf<String, Int>().also { handle[KEY_SELECT_IMAGE_MAP] = it }
    }

    val searchImages: StateFlow<List<SearchImageListTypeModel>> = handle.getStateFlow(KEY_SEARCH_IMAGE_LIST, emptyList())

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

    private val uiAction: PublishSubject<UiAction> = PublishSubject.create()
    private val uiActionFlow = MutableSharedFlow<UiAction>()


    init {
        bindAction()
        if(searchImages.value.isEmpty()) {
            uiAction.onNext(UiAction.Search(lastQuery ?: ""))
        }
    }

    private fun bindAction(){
        uiAction
            .filter { it is UiAction.Search }
            .cast(UiAction.Search::class.java)
            .flatMap {
                viewModelScope.launch {
                    _uiEvent.emit(UiEvent.KeyboardVisibleEvent(false))
                }
                when (dataLoading.value) {
                    true -> {
                        Observable.just(
                            Result.failure(Throwable(resourceProvider.getString(StringResourceProvider.StringResourceId.Loading)))
                        )
                    }
                    else -> {
                        if(it.query != lastQuery && selectImageUrlMap.isNotEmpty()) {
                            selectImageUrlMap.clear()
                            setHeaderTitleUseSelectMap()
                        }
                        lastQuery = it.query
                        currentPage = 1
                        _dataLoading.value = true
                        fetchSearchDataQueryDataUseCase(it.query, 1)
                    }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("search query subscribe => $it")
                processSearchResult(it)
            }.addTo(compositeDisposable)

        uiAction
            .filter { it is UiAction.Paging }
            .cast(UiAction.Paging::class.java)
            .flatMap {
                when {
                    it.query.isNullOrBlank() -> Observable.empty()
                    pagingDataLoading.value -> Observable.empty()
                    else -> {
                        _pagingDataLoading.value = true
                        fetchSearchDataQueryDataUseCase(it.query, it.page)
                    }
                }
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe{ res ->
                processPagingResult(res)
            }.addTo(compositeDisposable)

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

        viewModelScope.launch {
            uiActionFlow
                .filterIsInstance<UiAction.ClickImageNoneSelectModeEvent>()
                .throttleFirst(500)
                .collect {
                    _uiEvent.emit(UiEvent.NavigateImageDetail(it.imageUrl, it.position))
                }
        }
    }

    private fun processSearchResult(res: Result<List<SearchImageListTypeModel>>){
        _dataLoading.value = false
        res.onSuccess {
            if (lastQuery?.isNotEmpty() == true) _searchResultIsEmpty.value = it.size <= 1
            when (selectImageUrlMap.isEmpty()) {
                true -> {
                    handle[KEY_NOTIFY_TEXT] = resourceProvider.getString(StringResourceProvider.StringResourceId.EmptySearchResult)
                    handle[KEY_SEARCH_IMAGE_LIST] = it
                }
                else -> {
                    handle[KEY_SEARCH_IMAGE_LIST] = it.map { item ->
                        when {
                            item is SearchImageListTypeModel.Image &&
                                    selectImageUrlMap.containsKey(item.image.imageUrl) ->
                                item.copy(image = item.image.copy(isSelect = true))
                            else -> item
                        }
                    }
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
                                lastQuery?.let { query ->
                                    uiAction.onNext(UiAction.Search(query))
                                }
                            }
                        )
                        handle[KEY_NOTIFY_TEXT] = msg
                    }
                }
            }
        }
    }

    private fun processPagingResult(res: Result<List<SearchImageListTypeModel>>) {
        _pagingDataLoading.value = false
        res.onSuccess {
            currentPage++
            handle[KEY_SEARCH_IMAGE_LIST] = searchImages.value + it
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
                showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SaveSuccess))
                clickSelectModeEvent()
            }
            else -> showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SaveFail))
        }
    }

    private fun processSaveImageException(throwable: Throwable) {
        _dataLoading.value = false
        throwable.printStackTrace()
        showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SaveFail) + " $throwable")
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
        handle[KEY_SEARCH_IMAGE_LIST] = images
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
            handle[KEY_SEARCH_IMAGE_LIST] = images
            setHeaderTitleUseSelectMap()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SelectFail))
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
                uiActionFlow.emit((UiAction.ClickImageNoneSelectModeEvent(image.imageUrl, idx)))
            }
        }
    }

    fun searchQueryEvent(query: String) {
        uiAction.onNext(UiAction.Search(query))
    }

    fun fetchNextPage() {
        uiAction.onNext(UiAction.Paging(lastQuery, currentPage + 1))
    }

    fun touchToolBarEvent() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ScrollToTop(currentPage == 1))
        }
    }

    private fun showToast(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowToast(message))
        }
    }

    private fun showSnackBar(message: String, action: Pair<String, () -> Unit>?) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowSnackBar(message, action))
        }
    }

    sealed class UiAction {
        data class Search(val query: String) : UiAction()
        data class Paging(val query: String?, val page: Int) : UiAction()
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
