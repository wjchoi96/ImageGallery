package com.gallery.kakaogallery.presentation.ui.gallery

import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.FragmentGalleryBinding
import com.gallery.kakaogallery.presentation.extension.setSoftKeyboardVisible
import com.gallery.kakaogallery.presentation.extension.showToast
import com.gallery.kakaogallery.presentation.ui.base.DisposableManageFragment
import com.gallery.kakaogallery.presentation.util.DialogUtil
import com.gallery.kakaogallery.presentation.viewmodel.GalleryViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class GalleryFragment : DisposableManageFragment<FragmentGalleryBinding>() {
    override val layoutResId: Int
        get() = R.layout.fragment_gallery
    private val viewModel: GalleryViewModel by viewModels()

    private val galleryAdapter: GalleryAdapter by lazy { GalleryAdapter(viewModel::touchImageEvent) }
    private var itemCount: Int = 3

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
        setRecyclerView()
        setSwipeRefreshListener()
    }


    private fun initHeader() {
        binding.layoutToolbar.let {
            it.tvBtnRight.isVisible = false
            it.tvBtnLeft.isVisible = false
            it.toolBar.setOnClickListener {
                scrollToTop()
            }
        }
    }

    private fun startSelectMode() {
        binding.layoutToolbar.let {
            it.tvBtnLeft.apply {
                isVisible = true
                text = getString(R.string.remove)
                setOnClickListener {
                    viewModel.clickRemoveEvent()
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

    private fun setRecyclerView() {
        val viewManager = GridLayoutManager(mContext, itemCount)
        binding.rvGallery.apply {
            layoutManager = viewManager
            adapter = galleryAdapter
            addItemDecoration(itemDecoration)
        }
    }

    private val itemDecoration = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            val position = parent.getChildAdapterPosition(view)
            val spacing = getPxFromDp(10).toInt() //20 // px // 간격
            val column = position % itemCount // 현재 col

            // 0 -> 왼오, 1 -> 오, 2 -> 오
            outRect.left =
                spacing - column * spacing / itemCount // spacing - column * ((1f / spanCount) * spacing)
            outRect.right =
                (column + 1) * spacing / itemCount // (column + 1) * ((1f / spanCount) * spacing)

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

    private fun setSwipeRefreshListener() {
        binding.layoutSwipeRefresh.setOnRefreshListener {
            viewModel.fetchSaveImages()
        }
        binding.layoutSwipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    private fun scrollToTop() {
        if (galleryAdapter.currentItemSize != 0) {
            binding.rvGallery.smoothScrollToPosition(0)
        }
    }

    private fun showRemoveDialog(selectCount: Int) {
        DialogUtil.showBottom(
            mContext ?: return,
            getString(R.string.message_is_remove_select_image, selectCount),
            getString(R.string.remove),
            getString(R.string.cancel),
            {
                viewModel.removeSelectImage()
            }) {}
    }

    private fun observeData() {
        viewModel.uiEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                when (it) {
                    is GalleryViewModel.UiEvent.ShowToast ->
                        mContext?.showToast(it.message)
                    is GalleryViewModel.UiEvent.KeyboardVisibleEvent ->
                        mContext?.setSoftKeyboardVisible(binding.background, it.visible)
                    is GalleryViewModel.UiEvent.PresentRemoveDialog ->
                        showRemoveDialog(it.selectCount)
                }
            }
        }

        viewModel.saveImages.observe(this) {
            Timber.d("savedImageListObservable subscribe thread - " + Thread.currentThread().name)
            for ((idx, i) in it.withIndex()) {
                Timber.d("[" + idx + "] : " + i.toMinString())
            }
            galleryAdapter.updateList(it)
            setEmptyListView()
        }

        viewModel.dataLoading.observe(this) {
            binding.progress.isVisible = it
            if(!it)
                finishRefresh()
        }

        viewModel.headerTitle.observe(this) {
            binding.layoutToolbar.toolBar.title = it
        }

        viewModel.selectMode.observe(this) {
            Timber.d("select mode debug at observe -> $it")
            when (it){
                true -> startSelectMode()
                else -> finishSelectMode()
            }
        }
    }

    private fun setEmptyListView() {
//        if (viewModel.imageList.isEmpty())
//            binding.tvNoneNotify.visibility = View.VISIBLE
//        else
//            binding.tvNoneNotify.visibility = View.GONE
    }

    private fun finishRefresh() {
        if (binding.layoutSwipeRefresh.isRefreshing)
            binding.layoutSwipeRefresh.isRefreshing = false
    }
}