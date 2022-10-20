package com.gallery.kakaogallery.presentation.ui.searchimage

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.FragmentSearchImageBinding
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.presentation.extension.hideKeyboard
import com.gallery.kakaogallery.presentation.extension.showToast
import com.gallery.kakaogallery.presentation.ui.base.BaseFragmentUseHandler
import com.gallery.kakaogallery.presentation.util.DialogUtil
import com.gallery.kakaogallery.presentation.viewmodel.SearchImageViewModel
import dagger.hilt.android.AndroidEntryPoint

/*
    보관된 이미지는 특별한 표시를 보여줍니다. (좋아요/별표/하트 등)
    응답 이미지마다 보관된 이미지인지 체크를 해야하나?

    LiveData 설명 잘되어있다
    https://comoi.io/300

    ListAdapter Issue
    https://bb-library.tistory.com/257 새로운 List 가 필요한 이슈

 */

@AndroidEntryPoint
class SearchImageFragment : BaseFragmentUseHandler<FragmentSearchImageBinding>() {
    override val layoutResId: Int
        get() = R.layout.fragment_search_image

    //https://kotlinworld.com/87
    //같은 ViewModelStoreOwner(Acitivty, Fragment)에 대해 같은 이름의 ViewModel 클래스를 get하면 같은 인스턴스가 반환된다.
    private val viewModel: SearchImageViewModel by viewModels()

    private val imageSearchAdapter: SearchImagesAdapter by lazy { SearchImagesAdapter(viewModel) }

    private var itemCount = 3

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Log.d("TAG", "search onHiddenChanged => $hidden")
        if (!hidden) {
            fHandler?.getHeaderCompFromRoot()?.setBackgroundClickListener { scrollToTop() }
            setSelectMode(viewModel.selectMode.value == true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView(view)
        observeData()
    }

    private fun initData() {
        itemCount = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> 3
            Configuration.ORIENTATION_LANDSCAPE -> 5
            else -> 3
        }
    }

    private fun initView(root: View) {
        Log.d("TAG", "initView => isHidden : $isHidden")
        initHeader()
        setupRecyclerView()
        setListener()
    }

    // replace 를 안해주니까 header comp 도 날아가는구나
    // resume 에서 처리해줘야하나?
    private fun initHeader() {
        if (isHidden) //  현재 보이는 fragment 가 header setting 에 우선권을 가지도록 설정
            return
        // 상대 fragment 탭의 onStop 이후로 여기보다 먼저 호출되는 파괴 관련 생명주기 콜백이 없다
        // onStop 에서 header button 을 제거할 수는 없으니, init code 에서 처리하자
        fHandler?.getHeaderCompFromRoot()?.apply {
            clearView()
            setBackgroundClickListener {
                scrollToTop()
            }
        }
        Log.d("TAG", "init header ${viewModel.selectMode.value}")
        setSelectMode(viewModel.selectMode.value == true)
    }

    private fun setSelectMode(selectMode: Boolean) {
        if (selectMode)
            startSelectMode()
        else
            finishSelectMode()
    }

    private fun startSelectMode() {
        fHandler?.getHeaderCompFromRoot()?.apply {
            setLeftBtnListener("취소") {
                viewModel.setSelectMode(false)
            }
            setRightBtnListener("저장") {
                showSaveDialog()
            }
            setTitle("0장 선택중")
        }
    }

    private fun finishSelectMode() {
        fHandler?.getHeaderCompFromRoot()?.apply {
            removeLeftBtn()
            setRightBtnListener("선택") {
                viewModel.setSelectMode(true)
            }
            setTitle("이미지 검색")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListener() {
        binding.recyclerView.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_UP -> mContext?.hideKeyboard(v)
            }
            return@setOnTouchListener false
        }
    }

    private fun setupRecyclerView() {
        val viewManager =
            GridLayoutManager(mContext, itemCount, GridLayoutManager.VERTICAL, false).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (imageSearchAdapter.getItemViewType(position) == SearchImagesAdapter.SearchQueryType)
                            this@SearchImageFragment.itemCount
                        else
                            1
                    }
                }
            }
        binding.recyclerView.apply {
            layoutManager = viewManager
            adapter = imageSearchAdapter
            addItemDecoration(itemDecoration)
            addOnScrollListener(pagingListener)
        }
        imageSearchAdapter.setList(ArrayList())
        imageSearchAdapter.notifyDataSetChanged()
    }

    // https://medium.com/@bigstark/recyclerview-grid-space-%EC%97%90-%EB%8C%80%ED%95%9C-%EA%B3%A0%EC%B0%B0-7cab7a725f98
    // https://stackoverflow.com/questions/28531996/android-recyclerview-gridlayoutmanager-column-spacing => !!
    private val itemDecoration = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            if (parent.getChildAdapterPosition(view) == 0) { // 검색 section
                return
            }
            val position = parent.getChildAdapterPosition(view) - 1 // search query section 때문
            val spacing = getPxFromDp(10).toInt() //20 // px // 간격
            val column = position % itemCount // 현재 col

            // 0 -> 왼오, 1 -> 오, 2 -> 오
            outRect.left = spacing - column * spacing / itemCount // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / itemCount // (column + 1) * ((1f / spanCount) * spacing)

            if (position < itemCount) { // top edge
                outRect.top = spacing
            }
            outRect.bottom = spacing * 2 // item bottom
        }
    }

    fun getPxFromDp(dp: Int): Float {
        val displayMetrics = resources.displayMetrics
        return dp * (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    private val pagingListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (!recyclerView.canScrollVertically(1)) { // direction 은 Vertically 기준으로 -1이 위쪽, 1이 아래쪽이다
                Log.d("TAG", "vertical end")
                if (imageSearchAdapter.currentItemSize != 0) {
                    viewModel.fetchNextPage()
                }
            }
        }
    }

    private fun scrollToTop() {
        if (imageSearchAdapter.currentItemSize != 0) {
            binding.recyclerView.smoothScrollToPosition(0)
        }
    }

    private fun showSaveDialog() {
//        if(viewModel.selectImageIdxList.isEmpty()){
//            showToast("이미지를 선택해주세요")
//            return
//        }
        //${viewModel.selectImageIdxList.size}장의 이미지를 저장하시겠습니까?
        DialogUtil.showBottom(mContext ?: return, "선택한 이미지를 저장하시겠습니까?", "저장", "취소", {
            viewModel.saveSelectImage()
        }) {}
    }

    private fun observeData() {
        viewModel.searchImages.observe(this) {
            Log.d(
                "TAG",
                "searchResultObservable subscribe thread - ${Thread.currentThread().name}, it.address : $it"
            )
            processImages(it.first, it.second)
        }

        viewModel.searchImagesUseDiff.observe(this) {
            Log.d("TAG", "diff debug searchImagesUseDiff observe")
            imageSearchAdapter.updateList(it)
        }

        viewModel.toastText.observe(this) {
            mContext?.showToast(it)
        }

        viewModel.dataLoading.observe(this) {
            binding.progress.isVisible = it
        }
        viewModel.pagingDataLoading.observe(this) {
            binding.listProgress.isVisible = it
        }

        viewModel.headerTitle.observe(this) {
            fHandler?.getHeaderCompFromRoot()?.apply { setTitle(it) }
        }

        viewModel.keyboardShownEvent.observe(this) {
            if (it == false) {
                mContext?.hideKeyboard(binding.background)
            }
        }

        viewModel.selectMode.observe(this) {
            setSelectMode(it)
        }
    }

    /**
     * 여기는 search view holder 를 get item count 에 +1 를 하는 방식으로 구현했을때 기준
     * idx 값에 +1 이 되어서 처리되어있을것
     * 일단 수정은 하지 않았다
     */
    private fun processImages(images: List<ImageModel>, payload: ImageModel.Payload) {
        when (payload.payloadType) {
            ImageModel.Payload.PayloadType.NewList -> {
                imageSearchAdapter.setList(images)
                imageSearchAdapter.notifyDataSetChanged()
                imageSearchAdapter.setLastQuery(viewModel.lastQuery.value) // testCode
                if (images.isEmpty())
                    binding.tvNoneNotify.visibility = View.VISIBLE
                else
                    binding.tvNoneNotify.visibility = View.GONE
            }
            ImageModel.Payload.PayloadType.Inserted -> {}
            ImageModel.Payload.PayloadType.InsertedRange -> {
                // empty 의 경우는 viewmodel에서 알아서 toast, dataload 등을 통해서 처리한다
                // 넘어오는경우는 무조건 변경이 있을때
                val positionStart =
                    1 + images.size - payload.changedIdx.size // or payload.changedIdx[0]
                Log.d("TAG", "pagingListObservable : $positionStart - ${payload.changedIdx.size}")
                imageSearchAdapter.setList(images)
                imageSearchAdapter.notifyItemRangeInserted(positionStart, payload.changedIdx.size)
            }
            ImageModel.Payload.PayloadType.Removed -> {}
            ImageModel.Payload.PayloadType.Changed -> {
                if (payload.changedPayload == null)
                    return
                imageSearchAdapter.setList(images)
                when (payload.changedPayload) {
                    ImageModel.Payload.ChangedType.Save -> {
                        for (idx in payload.changedIdx) {
                            imageSearchAdapter.notifyItemChanged(
                                idx + 1,
                                SearchImagesAdapter.ImagePayload.Save
                            )
                        }
                        // add save, remove save 두종류가 있다
                        setSelectMode(false) // 이건 add save 처리에만 필요한거긴 한데 일단 배치해보자
                        // select 중인 이미지가 삭제되었을때? selectMode가 종료되는데, viewModel 에서는 종료되지않았다
                    }
                    ImageModel.Payload.ChangedType.Select -> {
                        for (idx in payload.changedIdx) {
                            imageSearchAdapter.notifyItemChanged(
                                idx + 1,
                                SearchImagesAdapter.ImagePayload.Select
                            )
                        }
                    }
                }
            }
        }

    }
}

/*
 // imageList, changedIdx, changedPayload
//        viewModel.savedImageIdxListObservable.subscribe {
//            setProgress(false)
//            imageSearchAdapter.setList(viewModel.imageList, viewModel.lastQuery)
//            for(idx in it){
////                Log.d(TAG, "saved image : ${viewModel.imageList[idx].isSaveImage}, ${viewModel.imageList[idx].saveDateTime ?: viewModel.imageList[idx].dateTime}")
//                imageSearchAdapter.notifyItemChanged(idx + 1,
//                    SearchImagesAdapter.ImagePayload.Save
//                )
//            }
//            finishSelectMode()
//        }.apply { compositeDisposable.add(this) }

        // image list, inserted count
//        viewModel.pagingListObservable.subscribe {
//            setProgressPaging(false)
//            if(it == 0)
//                showToast("마지막 페이지입니다")
//            else if(it > 0){ // search header 가 존재해서 + 1
//                val positionStart = 1 + viewModel.imageList.size - it
//                Log.d(TAG, "pagingListObservable : $positionStart - $it")
//                imageSearchAdapter.setList(viewModel.imageList, viewModel.lastQuery)
//                imageSearchAdapter.notifyItemRangeInserted(positionStart, it)
//            } // it < 0 => network error or server error
//        }.apply { compositeDisposable.add(this) }

        // imageList, removeIdx, changedPayload
//        viewModel.removedImageIdxListObservable.subscribe {
//            imageSearchAdapter.setList(viewModel.imageList, viewModel.lastQuery)
//            for(idx in it){
////                Log.d(TAG, "removed image : ${viewModel.imageList[idx].isSaveImage}, ${viewModel.imageList[idx].saveDateTime ?: viewModel.imageList[idx].dateTime}")
//                imageSearchAdapter.notifyItemChanged(idx + 1, SearchImagesAdapter.ImagePayload.Save)
//            }
//        }.apply { compositeDisposable.add(this) }
 */