package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.MaxPageException
import com.gallery.kakaogallery.domain.repository.ImageRepository
import com.gallery.kakaogallery.domain.usecase.FetchSaveImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val fetchSaveImageUseCase: FetchSaveImageUseCase
) : DisposableManageViewModel() {

    private val fetchImageFailMessage: String = "이미지를 불러올수 없습니다"
    private val selectImageHashMap = mutableMapOf<String, Int>()

    /**
     * live data for data
     */
    private val _saveImages = MutableLiveData<List<ImageModel>>()
    val saveImages: LiveData<List<ImageModel>> = _saveImages

    private val _headerTitle = MutableLiveData("내 보관함")
    var headerTitle: LiveData<String> = _headerTitle

    private val _selectMode = MutableLiveData(false)
    val selectMode: LiveData<Boolean> = _selectMode

    /**
     * live data for event
     */
    private val _toastText = MutableLiveData<String>()
    val toastText: LiveData<String> = _toastText

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _keyboardShownEvent = MutableLiveData<Boolean>()
    val keyboardShownEvent: LiveData<Boolean> = _keyboardShownEvent

    init {
        fetchSaveImages()
    }

    fun removeSelectImage(){
        _dataLoading.value = true
        removeSaveImageUseCase(selectImageHashMap)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { res ->
                _dataLoading.value = false
                res.onSuccess {
                    when(it){
                        true -> {
                            showToast("삭제 성공")
                            clickSelectModeEvent() // 삭제라면 선택모드 였을테니, toggle 해주면 선택모드가 해제됨
                        }
                        else -> showToast("삭제 실패")
                    }
                }.onFailure {
                    it.printStackTrace()
                    showToast("삭제 실패 $it")
                }
            }.addTo(compositeDisposable)
    }

    fun fetchSaveImages() {
        _dataLoading.value = true
        fetchSaveImageUseCase()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { res ->
                _dataLoading.value = false
                res.onSuccess {
                    _saveImages.value = it
                }.onFailure {
                    when (it) {
                        is MaxPageException -> showToast("마지막 페이지입니다")
                        else -> showToast("$fetchImageFailMessage\n${it.message}")
                    }
                }
            }.addTo(compositeDisposable)
    }

    fun clickSelectModeEvent(){
        when(_selectMode.value){
            true -> {
                unSelectAllImage()
                _headerTitle.value = "내 보관함"
            }
            else -> _headerTitle.value = "${selectImageHashMap.size}장 선택중"
        }
        _selectMode.value = !(_selectMode.value ?: false)
    }

    fun touchImageEvent(image: ImageModel, idx: Int){
        _keyboardShownEvent.value = false
        when (selectMode.value){
            true ->
                setSelectImage(image, idx, !selectImageHashMap.containsKey(image.hash))
            else -> {}
        }
    }

    private fun unSelectAllImage() {
        val images = saveImages.value?.toMutableList() ?: return
        try {
            for(idx in selectImageHashMap.values){
                images[idx] = images[idx].copy(isSelect = false)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        selectImageHashMap.clear()
        _saveImages.value = images
    }

    private fun setSelectImage(image: ImageModel, idx: Int, select: Boolean) {
        val images = saveImages.value?.toMutableList() ?: return
        try {
            images[idx] = images[idx].copy(isSelect = select)
            when (select){
                true -> selectImageHashMap[image.hash] = idx
                else -> selectImageHashMap.remove(image.hash)
            }
            _saveImages.value = images
            _headerTitle.value = "${selectImageHashMap.size}장 선택중"
        }catch (e: Exception){
            e.printStackTrace()
            showToast("선택 실패")
        }
    }

    private fun showToast(message: String){
        _toastText.value = message
    }
}