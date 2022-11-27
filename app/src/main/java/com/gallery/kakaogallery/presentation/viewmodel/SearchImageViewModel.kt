package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.gallery.kakaogallery.domain.model.SearchImageListTypeModel
import com.gallery.kakaogallery.domain.usecase.FetchQueryDataUseCase
import com.gallery.kakaogallery.domain.usecase.SaveSelectImageUseCase
import com.gallery.kakaogallery.presentation.application.StringResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.PublishSubject
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
    }
    private var page: Int= handle[KEY_CURRENT_PAGE] ?: 1
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

    private val _searchImages: MutableLiveData<List<SearchImageListTypeModel>> =
        handle.getLiveData(KEY_SEARCH_IMAGE_LIST, emptyList())
    val searchImages: LiveData<List<SearchImageListTypeModel>> = _searchImages

    private val _searchResultIsEmpty = MutableLiveData(false)
    val searchResultIsEmpty: LiveData<Boolean> = _searchResultIsEmpty

    private val _headerTitle: MutableLiveData<String> =
        handle.getLiveData(KEY_HEADER_TITLE, resourceProvider.getString(StringResourceProvider.StringResourceId.MenuSearchImage))
    override val headerTitle: LiveData<String> = _headerTitle

    private val _selectMode: MutableLiveData<Boolean> = handle.getLiveData(KEY_SELECT_MODE, false)
    val selectMode: LiveData<Boolean> = _selectMode

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _pagingDataLoading = MutableLiveData(false)
    val pagingDataLoading: LiveData<Boolean> = _pagingDataLoading

    private val _uiEvent = MutableLiveData<SingleEvent<UiEvent>>()
    val uiEvent: LiveData<SingleEvent<UiEvent>> = _uiEvent

    private val _uiAction: PublishSubject<UiAction> = PublishSubject.create()


    init {
        bindAction()
        if(_searchImages.value == null || _searchImages.value!!.isEmpty()){
            _uiAction.onNext(UiAction.Search(lastQuery ?: ""))
        }
    }

    private fun bindAction(){
        _uiAction
            .filter { it is UiAction.Search }
            .flatMap {
                with (it as UiAction.Search) {
                    _uiEvent.value = SingleEvent(UiEvent.KeyboardVisibleEvent(false))
                    when (dataLoading.value) {
                        true -> {
                            Observable.just(
                                Result.failure(Throwable(resourceProvider.getString(StringResourceProvider.StringResourceId.Loading)))
                            )
                        }
                        else -> {
                            if(query != lastQuery && selectImageUrlMap.isNotEmpty()) {
                                selectImageUrlMap.clear()
                                setHeaderTitleUseSelectMap()
                            }
                            lastQuery = it.query
                            page = 1
                            _dataLoading.value = true
                            fetchSearchDataQueryDataUseCase(it.query, 1)
                        }
                    }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("search query subscribe => $it")
                processSearchResult(it)
            }.addTo(compositeDisposable)

        _uiAction
            .filter { it is UiAction.Paging }
            .flatMap {
                with (it as UiAction.Paging) {
                    when {
                        it.query.isNullOrBlank() -> Observable.empty()
                        pagingDataLoading.value == true -> Observable.empty()
                        else -> {
                            _pagingDataLoading.value = true
                            fetchSearchDataQueryDataUseCase(it.query, it.page)
                        }
                    }
                }
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe{ res ->
                processPagingResult(res)
            }.addTo(compositeDisposable)

        _uiAction
            .filter { it is UiAction.SaveSelectImage }
            .flatMapSingle {
                _dataLoading.value = true
                with (it as UiAction.SaveSelectImage) {
                    when (it.selectImageMap.isEmpty() || it.images == null) {
                        true ->
                            Single.error(Throwable(resourceProvider.getString(StringResourceProvider.StringResourceId.NoneSelectImage)))
                        else -> saveSelectImageUseCase(
                            it.selectImageMap,
                            it.images
                        )
                    }
                }
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                processSaveImageResult(it)
            }){
                processSaveImageException(it)
            }.addTo(compositeDisposable)

        _uiAction
            .filter { it is UiAction.ClickSaveAction }
            .subscribe {
                with (it as UiAction.ClickSaveAction) {
                    when (it.selectImageCount) {
                        0 -> _uiEvent.value =
                            SingleEvent(UiEvent.ShowToast(resourceProvider.getString(StringResourceProvider.StringResourceId.NoneSelectImage)))
                        else -> _uiEvent.value = SingleEvent(UiEvent.PresentSaveDialog(selectImageUrlMap.size))
                    }
                }
            }.addTo(compositeDisposable)
    }

    private fun processSearchResult(res: Result<List<SearchImageListTypeModel>>){
        _dataLoading.value = false
        res.onSuccess {
            page++
            if (lastQuery?.isNotEmpty() == true) _searchResultIsEmpty.value = it.size <= 1
            when (selectImageUrlMap.isEmpty()) {
                true -> _searchImages.value = it
                else -> {
                    _searchImages.value = it.map { item ->
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
                is MaxPageException -> showToast(
                    resourceProvider.getString(
                        StringResourceProvider.StringResourceId.LastPage
                    )
                )
                else -> showToast("$searchFailMessage\n${it.message}")
            }
        }
    }

    private fun processPagingResult(res: Result<List<SearchImageListTypeModel>>) {
        _pagingDataLoading.value = false
        res.onSuccess {
            page++
            val prevList = _searchImages.value ?: emptyList()
            _searchImages.value = prevList + it
        }.onFailure {
            when (it) {
                is MaxPageException -> showToast(
                    resourceProvider.getString(
                        StringResourceProvider.StringResourceId.LastPage
                    )
                )
                else -> showToast("$searchFailMessage\n${it.message}")
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
        _uiAction.onNext(UiAction.SaveSelectImage(
            selectImageUrlMap,
            searchImages.value
        ))
    }

    private fun fetchNextSearchQuery(query: String, searchPage: Int) {
        _pagingDataLoading.value = true
        fetchSearchDataQueryDataUseCase(query, searchPage)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ res ->
                _pagingDataLoading.value = false
                res.onSuccess {
                    page = searchPage + 1
                    val prevList = _searchImages.value ?: emptyList()
                    _searchImages.value = prevList + it
                }.onFailure {
                    when (it) {
                        is MaxPageException -> showToast(
                            resourceProvider.getString(
                                StringResourceProvider.StringResourceId.LastPage
                            )
                        )
                        else -> showToast("$searchFailMessage\n${it.message}")
                    }
                }
            }.let { compositeDisposable.add(it) }
    }

    fun backgroundTouchEvent() {
        _uiEvent.value = SingleEvent(UiEvent.KeyboardVisibleEvent(false))
    }

    fun clickSaveEvent() {
        _uiAction.onNext(UiAction.ClickSaveAction(selectImageUrlMap.size))
    }

    fun clickSelectModeEvent() {
        when (_selectMode.value) {
            true -> unSelectAllImage()
            else -> {}
        }
        setHeaderTitleUseSelectMap()
        _selectMode.value = !(_selectMode.value ?: false)
    }

    private fun setHeaderTitleUseSelectMap() {
        when (selectImageUrlMap.isEmpty()) {
            true -> _headerTitle.value =
                resourceProvider.getString(StringResourceProvider.StringResourceId.MenuSearchImage)
            else -> _headerTitle.value = resourceProvider.getString(
                StringResourceProvider.StringResourceId.SelectState,
                selectImageUrlMap.size
            )
        }
    }

    private fun unSelectAllImage() {
        val images = searchImages.value?.toMutableList() ?: return
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
        _searchImages.value = images
    }

    private fun setSelectImage(image: ImageModel, idx: Int, select: Boolean) {
        val images = searchImages.value?.toMutableList() ?: return
        try {
            images[idx] = (images[idx] as SearchImageListTypeModel.Image).let {
                it.copy(image = it.image.copy(isSelect = select))
            }
            when (select) {
                true -> selectImageUrlMap[image.imageUrl] = idx
                else -> selectImageUrlMap.remove(image.imageUrl)
            }
            _searchImages.value = images
            setHeaderTitleUseSelectMap()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SelectFail))
        }
    }


    fun touchImageEvent(image: ImageModel, idx: Int) {
        _uiEvent.value = SingleEvent(UiEvent.KeyboardVisibleEvent(false))
        when (selectMode.value) {
            true ->
                setSelectImage(image, idx, !selectImageUrlMap.containsKey(image.imageUrl))
            else -> {}
        }
    }

    fun searchQueryEvent(query: String) {
        _uiAction.onNext(UiAction.Search(query))
    }

    fun fetchNextPage() {
        _uiAction.onNext(UiAction.Paging(lastQuery, page))
//        if (lastQuery.isNullOrBlank()) {
//            return
//        }
//        if (pagingDataLoading.value == true) {
//            return
//        }
//        fetchNextSearchQuery(lastQuery!!, page)
    }

    private fun showToast(message: String) {
        _uiEvent.value = SingleEvent(UiEvent.ShowToast(message))
    }

    sealed class UiAction {
        data class Search(val query: String) : UiAction()
        data class Paging(val query: String?, val page: Int) : UiAction()
        data class ClickSaveAction(val selectImageCount: Int) : UiAction()
        data class SaveSelectImage(
            val selectImageMap: MutableMap<String, Int>,
            val images: List<SearchImageListTypeModel>?
        ) : UiAction()
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data class PresentSaveDialog(val selectCount: Int) : UiEvent()
        data class KeyboardVisibleEvent(val visible: Boolean) : UiEvent()
    }
}
