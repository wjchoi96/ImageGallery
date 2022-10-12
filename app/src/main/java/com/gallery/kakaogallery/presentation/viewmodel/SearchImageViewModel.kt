package com.gallery.kakaogallery.presentation.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gallery.kakaogallery.data.repository.ImageRepository
import com.gallery.kakaogallery.data.service.ImageSearchService
import com.gallery.kakaogallery.data.service.VideoSearchService
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.domain.model.Result
import com.gallery.kakaogallery.domain.model.ResultError
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication

/*
    1. image list 는 view model 에서 관리
     - 필요시 가져가서 사용할 수는 있다
    2. observable 리턴 결과들
     - list?
     - idx 정보?

    1. save 하는 이미지 리스트를 저장해서 가지고 있는다 ( idx + url )
    2. 새로 search 하면 해당 리스트 비운다
    3. 이미지 보관함에서 지워진 이미지들을 observe 하다가, tempSavedList 가 존재한다면 비교해서 지워진 이미지 탐색
    4. tempSavedList 에서 지워진 이미지가 존재한다면 해당 정보 list 로 onNext
 */
/**
 * live data + notify changed insert, removed 등 작업 수행하는 방법
 *
 * 1. DiffUtil 을 사용해서 최소한의 업데이트 수를 계산
 * - 두 목록간의 차이점을 찾고, 업데이트 되어야 할 목록을 반환해준다
 * https://blog.kmshack.kr/RecyclerView-DiffUtil%EB%A1%9C-%EC%84%B1%EB%8A%A5-%ED%96%A5%EC%83%81%ED%95%98%EA%B8%B0/
 * - 그런데 아무리 생각을해봐도, view model 에서는 list 가 변경되는 정보를 알고 있는데, 그걸 공유를 안해서 view 에서 다시 목록의 차이점을 계산한다는게 조금 이상한거같다.
 *
 * 2. view model 에서 필요에 따라 변경된 데이터의 정보를 내보내준다
 *  * - repository 에서 list 를 fetch 해온다 => db, network 작업이 될 수 있겠다. 중요한것은 늘 새로운 list 를 리턴( 새로운 객체 )
 * - view model, list 보관, 필요에 따라 liveData 에 등록해서 내보내준다
 * - view : adapter list 보관(할수밖에없다)
 *
 * 3. 일단 repository 를 만들까
 *
 * 4. event live data 와 data live data 를 잘 구분해보자
 * https://vagabond95.me/posts/live-data-with-event-issue/
 * => google 공식 repo 2개 소개(1개는 이미 보고있는것)
 */

class SearchImageViewModel : BaseViewModel() {
    private var page = 1

    private val searchFailMessage : String = "검색을 실패했습니다"

    private val selectImageIdxList : ArrayList<Int> = ArrayList()
    // select 해서 저장한 이미지들의 map
    // 이미지보관함에서 이미지를 지울때, 대응 가능한 이미지들은 대응해주기 위함
    private val tempSavedImageMap : HashMap<String, Int> = HashMap()

    /**
     * live data for data
     */
    // 이거를 ImageModels 만 넘기는게 아니라 Pair 를 통해 payload 도 함께 넘길까? nullable 로 해서
    private val _searchImages = MutableLiveData<Pair<List<ImageModel>, ImageModel.Payload>>()
    val searchImages : LiveData<Pair<List<ImageModel>, ImageModel.Payload>> = _searchImages

    private val _searchImagesUseDiff = MutableLiveData<List<ImageModel>>()
    val searchImagesUseDiff : LiveData<List<ImageModel>> = _searchImagesUseDiff

    private val _lastQuery = MutableLiveData<String>()
    val lastQuery : LiveData<String> = _lastQuery

    private val _headerTitle = MutableLiveData<String>()
    var headerTitle : LiveData<String> = _headerTitle

    /**
     * live data for event
     */
    private val _toastText = MutableLiveData<String>()
    val toastText : LiveData<String> = _toastText

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _pagingDataLoading = MutableLiveData<Boolean>()
    val pagingDataLoading: LiveData<Boolean> = _pagingDataLoading

    private val _keyboardShownEvent = MutableLiveData<Boolean>()
    val keyboardShownEvent: LiveData<Boolean> = _keyboardShownEvent

    private val _selectMode = MutableLiveData<Boolean>()
    val selectMode: LiveData<Boolean> = _selectMode

    /**
     * repository
     * 본래 생성자로 전달받아야하지만 일단 여기서 생성
     */
    private val imageRepository = ImageRepository(
        ImageSearchDataSourceImpl(KakaoGalleryApplication.mRetrofit.getService(ImageSearchService::class.java)),
        VideoSearchDataSourceImpl(KakaoGalleryApplication.mRetrofit.getService(VideoSearchService::class.java))
    )

    init {

    }

    /**
     * repository code
     */
    fun saveSelectImage(){
        if(selectImageIdxList.isEmpty()){
            showToast("이미지를 선택해주세요")
            return
        }
        _dataLoading.value = true
        val imageList = searchImages.value?.first ?: return
        Thread {
            Log.d(TAG, "requestSaveImage : ${selectImageIdxList.size} - thread : ${Thread.currentThread().name}")
            val saveImageList = ArrayList<ImageModel>()
            selectImageIdxList.sort() // idx 순으로 정렬
            for(idx in selectImageIdxList){
                tempSavedImageMap[imageList[idx].imageUrl] = idx
                saveImageList.add(imageList[idx].apply { isSelect = false })
            }
//            saveImageStorage.saveImageList(saveImageList)

            Handler(Looper.getMainLooper()).post {
                _dataLoading.value = false
                _searchImages.value = Pair(imageList, ImageModel.Payload(selectImageIdxList, ImageModel.Payload.PayloadType.Changed, ImageModel.Payload.ChangedType.Save))
            }
        }.start()
    }

    // query 를 비워서 보내면 에러뜬다
    private fun fetchSearchQuery(query: String){
        _lastQuery.value = query
        page = 1
        tempSavedImageMap.clear()
        _dataLoading.value = true
        imageRepository.fetchQueryData(query, page).subscribe {
            _dataLoading.value = false
            when(it){
                is Result.Success -> {
                    page++
                    _searchImagesUseDiff.value = it.data!!
                }
                is Result.Fail -> {
                    when(it.error){
                        ResultError.MaxPage -> showToast("마지막 페이지입니다")
                        else -> showToast("$searchFailMessage\n${it.error?.message}")
                    }
                }
            }
        }.apply { addDisposable(this) }
    }

    private fun fetchNextSearchQuery(query: String, searchPage : Int){
        _pagingDataLoading.value = true
        imageRepository.fetchQueryData(query, searchPage).subscribe {
            _pagingDataLoading.value = false
            when(it){
                is Result.Success -> {
                    page = searchPage+1
                    val prevList = searchImagesUseDiff.value ?: ArrayList()
                    _searchImagesUseDiff.value = prevList + it.data!!
                }
                is Result.Fail -> {
                    showToast("$searchFailMessage\n${it.error?.message}")
                }
            }
        }.apply { addDisposable(this) }
    }

    /**
     * Called by View
     */
    fun setSelectMode(selectMode : Boolean){
        if(!selectMode){
            releaseAllSelectImage()
        }
        _selectMode.value = selectMode
    }
    private fun releaseAllSelectImage(){
        val imageList = searchImages.value?.first?.let { ArrayList(it) } ?: return
        for(idx in selectImageIdxList){
            imageList[idx].isSelect = false
        }
        _searchImages.value = Pair(imageList, ImageModel.Payload(selectImageIdxList, ImageModel.Payload.PayloadType.Changed, ImageModel.Payload.ChangedType.Select))
        selectImageIdxList.clear()
    }

    /**
     * Called by Data Binding
     */
    fun selectImage(image : ImageModel, idx : Int){
        Log.d(TAG, "select image item : $idx, selectMode : ${selectMode.value}")
        val imageList = searchImages.value?.first?.let { ArrayList(it) } ?: return
        Log.d(TAG, "select image item : $idx, selectMode : ${selectMode.value}, imageList : ${imageList.size}")
        if(selectMode.value == true) {
            Log.d(TAG, "before imageList[$idx].isSelect = ${imageList[idx].isSelect}")
            imageList[idx] = imageList[idx].copy().apply {
                isSelect = !isSelect
            }
            Log.d(TAG, "after imageList[$idx].isSelect = ${imageList[idx].isSelect}")
            if(imageList[idx].isSelect){
                selectImageIdxList.add(idx)
            }else{
                selectImageIdxList.remove(idx) // 여기서 remove 를 해버리니까 select 해제 시 해당 idx 전달이 안되네
            }
            _headerTitle.value = "${selectImageIdxList.size}장 선택중"
            Log.d(TAG, "imageList address : $imageList")
            _searchImages.value = Pair(imageList, ImageModel.Payload(List(1){idx}, ImageModel.Payload.PayloadType.Changed, ImageModel.Payload.ChangedType.Select))
        }
        _keyboardShownEvent.value = false
    }

    fun searchQuery(query: String){
        Log.d(TAG, "search query : $query")
        _keyboardShownEvent.value = false
        if(query.isBlank()){
            showToast("검색어를 입력해주세요")
            return
        }
        if(dataLoading.value == true){
            showToast("로딩중")
            return
        }
        fetchSearchQuery(query)
    }

    fun fetchNextPage(){
        if(lastQuery.value == null){
            return
        }
        if(pagingDataLoading.value == true){
            return
        }
        if(!imageRepository.hasNextPage()){
            showToast("마지막 페이지입니다")
            return
        }
        fetchNextSearchQuery(lastQuery.value!!, page)
    }

    val searchEditorActionListener = object : TextView.OnEditorActionListener {
        //android:imeOptions="actionSearch" 설정해놔야지 search action 이 enter key 에 들어온다
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            when(actionId){
                EditorInfo.IME_ACTION_SEARCH -> {
                    searchQuery(v?.text.toString())
                    return true
                }
            }
            return false
        }
    }

    private fun showToast(message: String) {
        _toastText.value = message
    }
}
