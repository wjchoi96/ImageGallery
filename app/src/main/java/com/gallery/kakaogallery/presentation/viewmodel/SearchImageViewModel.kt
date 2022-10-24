package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gallery.kakaogallery.domain.model.ImageListTypeModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.gallery.kakaogallery.domain.usecase.FetchQueryDataUseCase
import com.gallery.kakaogallery.domain.usecase.SaveSelectImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchImageViewModel @Inject constructor(
    private val fetchSearchDataQueryDataUseCase: FetchQueryDataUseCase,
    private val saveSelectImageUseCase: SaveSelectImageUseCase
) : DisposableManageViewModel() {
    private var page = 1

    private val searchFailMessage: String = "검색을 실패했습니다"

    private val selectImageUrlMap = mutableMapOf<String, Int>()

    // select 해서 저장한 이미지들의 map
    // 이미지보관함에서 이미지를 지울때, 대응 가능한 이미지들은 대응해주기 위함
    private val tempSavedImageMap = mutableMapOf<String, Int>()

    /**
     * live data for data
     */
    private val _searchImages = MutableLiveData<List<ImageListTypeModel>>()
    val searchImages: LiveData<List<ImageListTypeModel>> = _searchImages

    private val _lastQuery = MutableLiveData<String>()
    val lastQuery: LiveData<String> = _lastQuery

    private val _headerTitle = MutableLiveData("이미지 검색")
    var headerTitle: LiveData<String> = _headerTitle

    /**
     * live data for event
     */
    private val _toastText = MutableLiveData<String>()
    val toastText: LiveData<String> = _toastText

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _pagingDataLoading = MutableLiveData<Boolean>()
    val pagingDataLoading: LiveData<Boolean> = _pagingDataLoading

    private val _keyboardShownEvent = MutableLiveData<Boolean>()
    val keyboardShownEvent: LiveData<Boolean> = _keyboardShownEvent

    private val _selectMode = MutableLiveData<Boolean>()
    val selectMode: LiveData<Boolean> = _selectMode

    init {
        fetchSearchQuery("")
    }

    /**
     * repository code
     */
    fun saveSelectImage() {
        if (selectImageUrlMap.isEmpty()) {
            showToast("이미지를 선택해주세요")
            return
        }
        _dataLoading.value = true
        val images = searchImages.value ?: return
        saveSelectImageUseCase(
            selectImageUrlMap,
            images.filterIsInstance(ImageListTypeModel.Image::class.java)
                .map { it.image }
        ).observeOn(AndroidSchedulers.mainThread())
            .subscribe { res ->
            _dataLoading.value = false
            res.onSuccess {
                when(it){
                    true -> {
                        showToast("저장 성공")
                        setSelectMode(false)
                    }
                    else -> showToast("저장 실패")
                }
            }.onFailure {
                it.printStackTrace()
                showToast("저장 실패 $it")
            }
        }.addTo(compositeDisposable)
    }

    // query 를 비워서 보내면 에러뜬다
    private fun fetchSearchQuery(query: String) {
        _lastQuery.value = query
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
                        is MaxPageException -> showToast("마지막 페이지입니다")
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
                        is MaxPageException -> showToast("마지막 페이지입니다")
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
            true -> _headerTitle.value = "${selectImageUrlMap.size}장 선택중"
            else -> {
                unSelectAllImage()
                _headerTitle.value = "이미지 검색"
            }
        }
        _selectMode.value = selectMode
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
            _headerTitle.value = "${selectImageUrlMap.size}장 선택중"
        }catch (e: Exception){
            e.printStackTrace()
            showToast("선택 실패")
        }
    }

    /**
     * Called by Data Binding
     */
    fun touchImageEvent(image: ImageModel, idx: Int) {
        _keyboardShownEvent.value = false
        when (selectMode.value){
            true ->
                setSelectImage(image, idx, !selectImageUrlMap.containsKey(image.imageUrl))
            else -> {}
        }
    }

    fun searchQuery(query: String) {
        Timber.d("search query : $query")
        _keyboardShownEvent.value = false
        if (query.isBlank()) {
            showToast("검색어를 입력해주세요")
            return
        }
        if (dataLoading.value == true) {
            showToast("로딩중")
            return
        }
        fetchSearchQuery(query)
    }

    fun fetchNextPage() {
        if (lastQuery.value == null) {
            return
        }
        if (pagingDataLoading.value == true) {
            return
        }
        fetchNextSearchQuery(lastQuery.value!!, page)
    }

    private fun showToast(message: String) {
        _toastText.value = message
    }
}
