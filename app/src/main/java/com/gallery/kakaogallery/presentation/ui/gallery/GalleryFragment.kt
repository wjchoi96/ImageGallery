package com.gallery.kakaogallery.presentation.ui.gallery

import android.app.ActivityOptions
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.FragmentGalleryBinding
import com.gallery.kakaogallery.presentation.extension.*
import com.gallery.kakaogallery.presentation.ui.base.BindingFragment
import com.gallery.kakaogallery.presentation.ui.dialog.ImageManageBottomSheetDialog
import com.gallery.kakaogallery.presentation.ui.dialog.ImageManageBottomSheetEventReceiver
import com.gallery.kakaogallery.presentation.ui.imagedetail.ImageDetailActivity
import com.gallery.kakaogallery.presentation.ui.root.BottomMenuRoot
import com.gallery.kakaogallery.presentation.viewmodel.GalleryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@FlowPreview
@AndroidEntryPoint
class GalleryFragment : BindingFragment<FragmentGalleryBinding>(), ImageManageBottomSheetEventReceiver {
    override val layoutResId: Int
        get() = R.layout.fragment_gallery
    private val viewModel: GalleryViewModel by viewModels()

    private val galleryAdapter: GalleryAdapter by lazy { GalleryAdapter(viewModel::touchImageEvent) }
    private var itemCount: Int = 3

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
        setSwipeRefreshLayout()
    }

    private fun bindView(){
        binding.viewModel = viewModel
        binding.layoutToolbar.viewModel = viewModel
        bindRecyclerView()
    }

    private fun initHeader() {
        binding.layoutToolbar.let {
            it.tvBtnRight.isVisible = false
            it.tvBtnLeft.isVisible = false
            it.toolBar.setOnClickListener {
                viewModel.touchToolBarEvent()
            }
        }
    }

    private fun setSwipeRefreshLayout() {
        binding.layoutSwipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }


    private fun bindRecyclerView() {
        binding.galleryGridLayoutManager = GridLayoutManager(context, itemCount)
        binding.galleryAdapter = galleryAdapter.apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
        binding.galleryItemDecoration = itemDecoration
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

    private fun getPxFromDp(dp: Int): Float {
        val displayMetrics = resources.displayMetrics
        return dp * (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    private fun observeData() {
        repeatOnStarted {
            launch {
                viewModel.uiEvent.collect {
                    when (it) {
                        is GalleryViewModel.UiEvent.ShowToast ->
                            context?.showToast(it.message)

                        is GalleryViewModel.UiEvent.ShowSnackBar -> {
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
                        is GalleryViewModel.UiEvent.KeyboardVisibleEvent ->
                            context?.setSoftKeyboardVisible(binding.background, it.visible)

                        is GalleryViewModel.UiEvent.PresentRemoveDialog ->
                            showRemoveDialog(it.selectCount)

                        is GalleryViewModel.UiEvent.NavigateSearchView ->
                            (requireActivity() as? BottomMenuRoot)?.navigateSearchTab()

                        is GalleryViewModel.UiEvent.ScrollToTop ->
                            binding.rvGallery.safeScrollToTop(it.smoothScroll)

                        is GalleryViewModel.UiEvent.NavigateImageDetail -> {
                            showImageDetailActivity(it.imageUrl, it.position)
                        }
                    }
                }
            }

            launch {
                viewModel.selectMode.collect {
                    Timber.d("select mode debug at observe -> $it")
                    when (it){
                        true -> startSelectMode()
                        else -> finishSelectMode()
                    }
                }
            }

            launch {
                viewModel.saveImages.collectLatest {
                    lifecycleScope.launch {
                        galleryAdapter.updateList(it)
                    }
                }
            }
        }
    }

    private fun showImageDetailActivity(imageUrl: String, viewPosition: Int) {
        val imageView = binding.rvGallery
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

    private fun showRemoveDialog(selectCount: Int) {
        ImageManageBottomSheetDialog.newInstance(
            getString(R.string.message_is_remove_select_image, selectCount),
            getString(R.string.remove),
            getString(R.string.cancel)
        ).show(childFragmentManager)
    }

    override fun onPositiveEventReceive() {
        viewModel.removeSelectImage()
    }

    override fun onNegativeEventReceive() {

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

}