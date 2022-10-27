package com.gallery.kakaogallery.presentation.ui.searchimage

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.FragmentSearchImageBinding
import com.gallery.kakaogallery.domain.model.ImageListTypeModel
import com.gallery.kakaogallery.presentation.extension.hideKeyboard
import com.gallery.kakaogallery.presentation.extension.showToast
import com.gallery.kakaogallery.presentation.ui.base.BindingFragment
import com.gallery.kakaogallery.presentation.util.DialogUtil
import com.gallery.kakaogallery.presentation.viewmodel.SearchImageViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SearchImageFragment : BindingFragment<FragmentSearchImageBinding>() {
    override val layoutResId: Int
        get() = R.layout.fragment_search_image

    //https://kotlinworld.com/87
    //같은 ViewModelStoreOwner(Activity, Fragment)에 대해 같은 이름의 ViewModel 클래스를 get 하면 같은 인스턴스가 반환된다.
    private val viewModel: SearchImageViewModel by viewModels()

    private val imageSearchAdapter: SearchImagesAdapter by lazy {
        SearchImagesAdapter(
            viewModel::searchQuery,
            searchEditorActionListener,
            viewModel::touchImageEvent
        )
    }

    private var itemCount = 3

    private val searchEditorActionListener = object : TextView.OnEditorActionListener {
        //android:imeOptions="actionSearch" 설정해놔야지 search action 이 enter key 에 들어온다
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    viewModel.searchQuery(v?.text.toString())
                    return true
                }
            }
            return false
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Timber.d("search onHiddenChanged => $hidden")
        if (!hidden) {
            binding.layoutToolbar.layoutAppBar.setOnClickListener {
                scrollToTop()
            }
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
        Timber.d("initView => isHidden : $isHidden")
        initHeader()
        setupRecyclerView()
        setListener()
    }

    private fun initHeader() {
        if (isHidden) //  현재 보이는 fragment 가 header setting 에 우선권을 가지도록 설정
            return
        binding.layoutToolbar.let {
            it.tvBtnLeft.isVisible = false
            it.tvBtnRight.isVisible = false
            it.toolBar.setOnClickListener {
                scrollToTop()
            }
        }
        Timber.d("init header " + viewModel.selectMode.value)
        setSelectMode(viewModel.selectMode.value == true)
    }

    private fun setSelectMode(selectMode: Boolean) {
        if (selectMode)
            startSelectMode()
        else
            finishSelectMode()
    }

    private fun startSelectMode() {
        binding.layoutToolbar.let {
            it.tvBtnLeft.apply {
                isVisible = true
                text = getString(R.string.save)
                setOnClickListener {
                    showSaveDialog(0)
                }
            }
            it.tvBtnRight.apply {
                isVisible = true
                text = getString(R.string.cancel)
                setOnClickListener {
                    viewModel.setSelectMode(false)
                }
            }
        }
    }

    private fun finishSelectMode() {
        binding.layoutToolbar.let {
            it.tvBtnLeft.apply {
                isVisible = false
            }
            it.tvBtnRight.apply {
                isVisible = true
                text = getString(R.string.select)
                setOnClickListener {
                    viewModel.setSelectMode(true)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListener() {
        binding.rvSearch.setOnTouchListener { v, event ->
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
                        return if (imageSearchAdapter.getItemViewType(position) == ImageListTypeModel.ViewType.Query.id)
                            this@SearchImageFragment.itemCount
                        else
                            1
                    }
                }
            }
        binding.rvSearch.apply {
            layoutManager = viewManager
            adapter = imageSearchAdapter
            addItemDecoration(itemDecoration)
            addOnScrollListener(pagingListener)
        }
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
                Timber.d("vertical end")
                if (imageSearchAdapter.currentItemSize != 0) {
                    viewModel.fetchNextPage()
                }
            }
        }
    }

    private fun scrollToTop() {
        if (imageSearchAdapter.currentItemSize != 0) {
            binding.rvSearch.smoothScrollToPosition(0)
        }
    }

    private fun showSaveDialog(selectCount: Int) {
//        if(viewModel.selectImageIdxList.isEmpty()){
//            showToast("이미지를 선택해주세요")
//            return
//        }
        //${viewModel.selectImageIdxList.size}장의 이미지를 저장하시겠습니까?
        DialogUtil.showBottom(mContext ?: return, getString(R.string.message_is_save_select_image, selectCount), getString(R.string.save), getString(R.string.cancel), {
            viewModel.saveSelectImage()
        }) {}
    }

    private fun observeData() {
        viewModel.searchImages.observe(this) {
            Timber.d("searchResultObservable subscribe thread - " + Thread.currentThread().name + ", it.address : " + it)
            Timber.d("diff debug searchImagesUseDiff observe")
            imageSearchAdapter.updateList(it)
        }

        viewModel.toastMessageEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                mContext?.showToast(it)
            }
        }

        viewModel.dataLoading.observe(this) {
            binding.progress.isVisible = it
        }

        viewModel.pagingDataLoading.observe(this) {
            binding.progressPaging.isVisible = it
        }

        viewModel.headerTitle.observe(this) {
            binding.layoutToolbar.toolBar.title = it
        }

        viewModel.keyboardShownEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                when (it){
                    false -> mContext?.hideKeyboard(binding.background)
                    else -> {}
                }
            }
        }

        viewModel.selectMode.observe(this) {
            Timber.d("select mode debug at observe -> $it")
            setSelectMode(it)
        }
    }
}