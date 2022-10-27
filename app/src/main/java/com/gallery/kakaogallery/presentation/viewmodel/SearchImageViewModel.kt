package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gallery.kakaogallery.domain.model.ImageListTypeModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.gallery.kakaogallery.domain.usecase.FetchQueryDataUseCase
import com.gallery.kakaogallery.domain.usecase.SaveSelectImageUseCase
import com.gallery.kakaogallery.presentation.application.StringResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchImageViewModel @Inject constructor(
    private val resourceProvider: StringResourceProvider,
    private val fetchSearchDataQueryDataUseCase: FetchQueryDataUseCase,
    private val saveSelectImageUseCase: SaveSelectImageUseCase
) : DisposableManageViewModel() {
    private var page = 1
    private var lastQuery: String? = null

    private val searchFailMessage: String = resourceProvider.getString(StringResourceProvider.StringResourceId.SearchFail)
    private val selectImageUrlMap = mutableMapOf<String, Int>()

    // select 해서 저장한 이미지들의 map
    // 이미지보관함에서 이미지를 지울때, 대응 가능한 이미지들은 대응해주기 위함
    private val tempSavedImageMap = mutableMapOf<String, Int>()

    private val _searchImages = MutableLiveData<List<ImageListTypeModel>>(emptyList())
    val searchImages: LiveData<List<ImageListTypeModel>> = _searchImages

    private val _headerTitle = MutableLiveData(resourceProvider.getString(StringResourceProvider.StringResourceId.MenuSearchImage))
    var headerTitle: LiveData<String> = _headerTitle

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _pagingDataLoading = MutableLiveData(false)
    val pagingDataLoading: LiveData<Boolean> = _pagingDataLoading

    private val _selectMode = MutableLiveData(false)
    val selectMode: LiveData<Boolean> = _selectMode

    private val _uiEvent = MutableLiveData<SingleEvent<UiEvent>>()
    val uiEvent: LiveData<SingleEvent<UiEvent>> = _uiEvent


    init {
        fetchSearchQuery("")
    }

    /**
     * repository code
     */
    fun saveSelectImage() {
        if (selectImageUrlMap.isEmpty()) {
            showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.NoneSelectImage))
            return
        }
        _dataLoading.value = true
        val images = searchImages.value ?: return
        saveSelectImageUseCase(
            selectImageUrlMap,
            images
        ).observeOn(AndroidSchedulers.mainThread())
            .subscribe { res ->
            _dataLoading.value = false
            res.onSuccess {
                when(it){
                    true -> {
                        showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SaveSuccess))
                        clickSelectModeEvent()
                    }
                    else -> showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SaveFail))
                }
            }.onFailure {
                it.printStackTrace()
                showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SaveFail) + " $it")
            }
        }.addTo(compositeDisposable)
    }

    // query 를 비워서 보내면 에러뜬다
    private fun fetchSearchQuery(query: String) {
        lastQuery = query
        page = 1
        tempSavedImageMap.clear()
        _dataLoading.value = true
        fetchSearchDataQueryDataUseCase(query, page)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { res ->
                _dataLoading.value = false
                res.onSuccess {
                    page++
                    _searchImages.value = it
                }.onFailure {
                    when (it) {
                        is MaxPageException -> showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.LastPage))
                        else -> showToast("$searchFailMessage\n${it.message}")
                    }
                }
            }.let { compositeDisposable.add(it) }
    }

    private fun fetchNextSearchQuery(query: String, searchPage: Int) {
        _pagingDataLoading.value = true
        fetchSearchDataQueryDataUseCase(query, searchPage)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { res ->
                _pagingDataLoading.value = false
                res.onSuccess {
                    page = searchPage + 1
                    val prevList = _searchImages.value ?: emptyList()
                    _searchImages.value = prevList + it
                }.onFailure {
                    when (it) {
                        is MaxPageException -> showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.LastPage))
                        else -> showToast("$searchFailMessage\n${it.message}")
                    }
                }
            }.let { compositeDisposable.add(it) }
    }

    /**
     * Called by View
     */
    fun setSelectMode(selectMode: Boolean) {
        when(selectMode){
            true -> _headerTitle.value = resourceProvider.getString(StringResourceProvider.StringResourceId.SelectState, selectImageUrlMap.size)
            else -> {
    fun clickSaveEvent(){
        if(selectImageUrlMap.isEmpty()){
            _uiEvent.value =
                SingleEvent(UiEvent.ShowToast(resourceProvider.getString(StringResourceProvider.StringResourceId.NoneSelectImage)))
            return
        }
        _uiEvent.value = SingleEvent(UiEvent.PresentSaveDialog(selectImageUrlMap.size))
    }

    fun clickSelectModeEvent(){
        when(_selectMode.value){
            true -> {
                unSelectAllImage()
                _headerTitle.value = resourceProvider.getString(StringResourceProvider.StringResourceId.MenuSearchImage)
            }
            else -> _headerTitle.value = resourceProvider.getString(
                StringResourceProvider.StringResourceId.SelectState,
                selectImageUrlMap.size
            )
        }
        _selectMode.value = !(_selectMode.value ?: false)
    }

    private fun unSelectAllImage() {
        val images = searchImages.value?.toMutableList() ?: return
        try {
            for(idx in selectImageUrlMap.values){
                images[idx] = (images[idx] as ImageListTypeModel.Image).let {
                    it.copy(image = it.image.copy(isSelect = false))
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        selectImageUrlMap.clear()
        _searchImages.value = images
    }

    private fun setSelectImage(image: ImageModel, idx: Int, select: Boolean) {
        val images = searchImages.value?.toMutableList() ?: return
        try {
            images[idx] = (images[idx] as ImageListTypeModel.Image).let {
                it.copy(image = it.image.copy(isSelect = select))
            }
            when (select){
                true -> selectImageUrlMap[image.imageUrl] = idx
                else -> selectImageUrlMap.remove(image.imageUrl)
            }
            _searchImages.value = images
            _headerTitle.value = resourceProvider.getString(StringResourceProvider.StringResourceId.SelectState, selectImageUrlMap.size)
        }catch (e: Exception){
            e.printStackTrace()
            showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.SelectFail))
        }
    }

    /**
     * Called by Data Binding
     */
    fun touchImageEvent(image: ImageModel, idx: Int) {
        _uiEvent.value = SingleEvent(UiEvent.KeyboardVisibleEvent(false))
        when (selectMode.value){
            true ->
                setSelectImage(image, idx, !selectImageUrlMap.containsKey(image.imageUrl))
            else -> {}
        }
    }

    fun searchQuery(query: String) {
        Timber.d("search query : $query")
        _uiEvent.value = SingleEvent(UiEvent.KeyboardVisibleEvent(false))
        if (query.isBlank()) {
            showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.NoneQuery))
            return
        }
        if (dataLoading.value == true) {
            showToast(resourceProvider.getString(StringResourceProvider.StringResourceId.Loading))
            return
        }
        fetchSearchQuery(query)
    }

    fun fetchNextPage() {
        if (lastQuery.isNullOrBlank()) {
            return
        }
        if (pagingDataLoading.value == true) {
            return
        }
        fetchNextSearchQuery(lastQuery!!, page)
    }

    private fun showToast(message: String) {
        _uiEvent.value = SingleEvent(UiEvent.ShowToast(message))
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data class PresentSaveDialog(val selectCount: Int): UiEvent()
        data class KeyboardVisibleEvent(val visible: Boolean): UiEvent()
    }
}
