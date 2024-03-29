package com.gallery.kakaogallery.presentation.ui.searchimage

import android.annotation.SuppressLint
import android.app.ActivityOptions
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
import com.gallery.kakaogallery.domain.model.SearchImageListTypeModel
import com.gallery.kakaogallery.presentation.extension.*
import com.gallery.kakaogallery.presentation.ui.base.BindingFragment
import com.gallery.kakaogallery.presentation.ui.dialog.ImageManageBottomSheetDialog
import com.gallery.kakaogallery.presentation.ui.dialog.ImageManageBottomSheetEventReceiver
import com.gallery.kakaogallery.presentation.ui.imagedetail.ImageDetailActivity
import com.gallery.kakaogallery.presentation.viewmodel.SearchImageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@FlowPreview
@AndroidEntryPoint
class SearchImageFragment : BindingFragment<FragmentSearchImageBinding>(),
    ImageManageBottomSheetEventReceiver {
    override val layoutResId: Int
        get() = R.layout.fragment_search_image

    //https://kotlinworld.com/87
    //같은 ViewModelStoreOwner(Activity, Fragment)에 대해 같은 이름의 ViewModel 클래스를 get 하면 같은 인스턴스가 반환된다.
    private val viewModel: SearchImageViewModel by viewModels()

    private val imageSearchAdapter: SearchImagesAdapter by lazy {
        SearchImagesAdapter(
            viewModel::searchQueryEvent,
            viewModel::queryChangedEvent,
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
                    viewModel.searchQueryEvent(v?.text.toString())
                    return true
                }
            }
            return false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView()
        observeData()
    }

    private fun initData() {
        itemCount = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> 3
            Configuration.ORIENTATION_LANDSCAPE -> 5
            else -> 3
        }
    }

    private fun initView() {
        bindView()
        initHeader()
        setListener()
    }

    private fun bindView(){
        binding.viewModel = viewModel
        binding.layoutToolbar.viewModel = viewModel
        bindRecyclerView()
    }

    private fun initHeader() {
        binding.layoutToolbar.let {
            it.tvBtnLeft.isVisible = false
            it.tvBtnRight.isVisible = false
            it.toolBar.setOnClickListener {
                viewModel.touchToolBarEvent()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListener() {
        binding.rvSearch.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_UP -> viewModel.backgroundTouchEvent()
            }
            return@setOnTouchListener false
        }
    }

    private fun bindRecyclerView() {
        binding.searchLayoutManager =
            GridLayoutManager(context, itemCount, GridLayoutManager.VERTICAL, false).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (imageSearchAdapter.getItemViewType(position) == SearchImageListTypeModel.ViewType.Query.id)
                            this@SearchImageFragment.itemCount
                        else
                            1
                    }
                }
            }
        binding.searchAdapter = imageSearchAdapter.apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
        binding.searchItemDecoration = itemDecoration
        binding.rvSearch.addOnScrollListener(pagingListener)
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

    private fun getPxFromDp(dp: Int): Float {
        val displayMetrics = resources.displayMetrics
        return dp * (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    private val pagingListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            // direction 은 Vertically 기준으로 -1이 위쪽, 1이 아래쪽
            when {
                !recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE -> {
                    recyclerView.adapter?.itemCount?.let {
                        if(it != 0){
                            viewModel.fetchNextPage()
                        }
                    }
                }
            }
        }

    }

    private fun observeData() {
        repeatOnStarted {
            launch {
                viewModel.uiEvent.collect {
                    when (it) {
                        is SearchImageViewModel.UiEvent.ShowToast ->
                            context?.showToast(it.message)

                        is SearchImageViewModel.UiEvent.ShowSnackBar -> {
                            when (it.action) {
                                null -> binding.background.showSnackBar(it.message)
                                else -> binding.background.showSnackBar(
                                    it.message,
                                    it.action.first to View.OnClickListener { _ ->
                                        it.action.second.invoke()
                                    }
                                )
                            }
                        }
                        is SearchImageViewModel.UiEvent.KeyboardVisibleEvent -> {
                            context?.setSoftKeyboardVisible(binding.background, it.visible)
                            if(!it.visible)
                                (binding.rvSearch.findViewHolderForAdapterPosition(0) as? SearchQueryViewHolder)?.clearFocus()
                        }

                        is SearchImageViewModel.UiEvent.PresentSaveDialog ->
                            showSaveDialog(it.selectCount)

                        is SearchImageViewModel.UiEvent.ScrollToTop ->
                            binding.rvSearch.safeScrollToTop(it.smoothScroll)

                        is SearchImageViewModel.UiEvent.NavigateImageDetail -> {
                            showImageDetailActivity(it.imageUrl, it.position)
                        }
                    }
                }
            }

            launch {
                viewModel.selectMode.collect {
                    Timber.d("select mode debug at observe -> $it")
                    when (it) {
                        true -> startSelectMode()
                        else -> finishSelectMode()
                    }
                }
            }

            launch {
                viewModel.searchImages.collectLatest {
                    Timber.d("searchResultObservable subscribe thread - " + Thread.currentThread().name + ", it.address : " + it)
                    Timber.d("diff debug searchImagesUseDiff observe")
                    imageSearchAdapter.updateList(it)
                }
            }
        }

    }

    private fun showImageDetailActivity(imageUrl: String, viewPosition: Int) {
        val imageView = binding.rvSearch
            .findViewHolderForLayoutPosition(viewPosition)?.itemView?.findViewById<View>(R.id.iv_image)
        startActivity(
            ImageDetailActivity.get(requireContext(), imageUrl),
            ActivityOptions.makeSceneTransitionAnimation(
                requireActivity(),
                imageView,
                ImageDetailActivity.VIEW_NAME_IMAGE_DETAIL
            ).toBundle()
        )
    }

    private fun showSaveDialog(selectCount: Int) {
        ImageManageBottomSheetDialog.newInstance(
            getString(R.string.message_is_save_select_image, selectCount),
            getString(R.string.save),
            getString(R.string.cancel)
        ).show(childFragmentManager)
    }

    override fun onPositiveEventReceive() {
        viewModel.saveSelectImage()
    }

    override fun onNegativeEventReceive() {

    }

    private fun startSelectMode() {
        binding.layoutToolbar.let {
            it.tvBtnLeft.apply {
                isVisible = true
                text = getString(R.string.save)
                setOnClickListener {
                    viewModel.clickSaveEvent()
                }
            }
            it.tvBtnRight.apply {
                isVisible = true
                text = getString(R.string.cancel)
                setOnClickListener {
                    viewModel.clickSelectModeEvent()
                }
            }
        }
    }

    private fun finishSelectMode() {
        binding.layoutToolbar.let {
            it.tvBtnLeft.isVisible = false
            it.tvBtnRight.apply {
                isVisible = true
                text = getString(R.string.select)
                setOnClickListener {
                    viewModel.clickSelectModeEvent()
                }
            }
        }
    }

}