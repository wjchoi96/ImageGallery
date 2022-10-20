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

    private var imageListAdapter: GalleryAdapter? = null
    private var itemCount = 3

    /*
        1. search view 에서 이미지 저장시 자동으로 현재 뷰에 적용
        2. 이미지 선택중에 새로운 이미지가 추가되면? -> select idx 안꼬이게 처리
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Timber.d("save onHiddenChanged => $hidden")
        if (!hidden) {
            binding.layoutToolbar.layoutAppBar.setOnClickListener {
                scrollToTop()
            }
            if (viewModel.selectMode)
                startSelectMode()
            else
                finishSelectMode()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView(view)
        observeData()
        requestSavedImageList()
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
        if (isHidden) //  현재 보이는 fragment 가 header setting 에 우선권을 가지도록 설정
            return
        binding.layoutToolbar.let {
            it.tvBtnRight.isVisible = false
            it.tvBtnLeft.isVisible = false
        }
        Timber.d("init header " + viewModel.selectMode)
        if (viewModel.selectMode)
            startSelectMode()
        else
            finishSelectMode()
    }

    private fun startSelectMode() {
        viewModel.selectMode = true
        binding.layoutToolbar.let {
            it.tvBtnLeft.apply {
                isVisible = true
                text = "삭제"
                setOnClickListener {
                    showRemoveDialog()
                }
            }
            it.tvBtnRight.apply {
                isVisible = true
                text = "취소"
                setOnClickListener {
                    releaseAllSelectImage()
                    finishSelectMode()
                }
            }
            it.toolBar.title = "0장 선택중"
        }
    }

    private fun finishSelectMode() {
        viewModel.selectImageIdxList.clear()
        viewModel.selectMode = false
        binding.layoutToolbar.let {
            it.tvBtnLeft.apply {
                isVisible = false
            }
            it.tvBtnRight.apply {
                isVisible = true
                text = "선택"
                setOnClickListener {
                    startSelectMode()
                }
            }
            it.toolBar.title = "내 보관함"
        }
    }

    private fun releaseAllSelectImage() {
        for (idx in viewModel.selectImageIdxList) {
            viewModel.imageList[idx].isSelect = false
            imageListAdapter?.notifyItemChanged(idx, GalleryAdapter.ImagePayload.Select)
        }
        viewModel.selectImageIdxList.clear()
    }

    private fun setRecyclerView() {
        imageListAdapter = GalleryAdapter(mContext ?: return) { image, idx ->
            Timber.d("select image item : " + idx + ", " + viewModel.selectMode)
            if (viewModel.selectMode) {
                viewModel.imageList[idx].isSelect = !viewModel.imageList[idx].isSelect
                Timber.d("viewModel.imageList[" + idx + "].isSelect = " + viewModel.imageList[idx].isSelect)
                if (viewModel.imageList[idx].isSelect) {
                    viewModel.selectImageIdxList.add(idx)
                    "${viewModel.selectImageIdxList.size}장 선택중".let {
                        binding.layoutToolbar.toolBar.title = it
                    }
                } else {
                    viewModel.selectImageIdxList.remove(idx)
                    "${viewModel.selectImageIdxList.size}장 선택중".let {
                        binding.layoutToolbar.toolBar.title = it
                    }
                }
                imageListAdapter?.notifyItemChanged(idx, GalleryAdapter.ImagePayload.Select)
            }
            return@GalleryAdapter viewModel.selectMode
        }
        val viewManager = GridLayoutManager(mContext, itemCount)
        binding.rvGallery.apply {
            layoutManager = viewManager
            adapter = imageListAdapter
            addItemDecoration(itemDecoration)
        }
        if (viewModel.imageList.isNotEmpty()) {
            imageListAdapter?.setList(viewModel.imageList)
            imageListAdapter?.notifyDataSetChanged()
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
            requestSavedImageList()
        }
        binding.layoutSwipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    private fun scrollToTop() {
        if (viewModel.imageList.isNotEmpty()) {
            binding.rvGallery.smoothScrollToPosition(0)
        }
    }

    private fun showRemoveDialog() {
        if (viewModel.selectImageIdxList.isEmpty()) {
            mContext?.showToast("이미지를 선택해주세요")
            return
        }
        DialogUtil.showBottom(
            mContext ?: return,
            "${viewModel.selectImageIdxList.size}개의 이미지를 삭제하시겠습니까?",
            "삭제",
            "취소",
            {
                requestRemoveSelectImage()
            }) {}
    }

    private fun requestRemoveSelectImage() {
        if (viewModel.selectImageIdxList.isEmpty()) {
            mContext?.showToast("이미지를 선택해주세요")
            return
        }
        setProgress(true)
        viewModel.requestRemoveImageList(viewModel.selectImageIdxList)
    }

    private fun requestSavedImageList() {
        setProgress(true)
        viewModel.requestSavedImageList()
    }

    private fun observeData() {
        viewModel.errorMessageObservable.subscribe {
            setProgress(false)
            mContext?.showToast(it)
        }.apply { compositeDisposable.add(this) }

        viewModel.savedImageListObservable.subscribe {
            Timber.d("savedImageListObservable subscribe thread - " + Thread.currentThread().name)
            for ((idx, i) in it.withIndex()) {
                Timber.d("[" + idx + "] : " + i.toMinString())
            }
            setProgress(false)
            imageListAdapter?.setList(it)
            imageListAdapter?.notifyDataSetChanged()
            setEmptyListView()
        }.apply { compositeDisposable.add(this) }

        viewModel.removeImageIdxListObservable.subscribe {
            setProgress(false)
            imageListAdapter?.setList(viewModel.imageList)
            if (it.size == 1)
                imageListAdapter?.notifyItemRemoved(it.first())
            else
                imageListAdapter?.notifyDataSetChanged()
            finishSelectMode()
            setEmptyListView()
        }.apply { compositeDisposable.add(this) }

        viewModel.insertedImageIdxListObservable.subscribe {
            imageListAdapter?.setList(viewModel.imageList)
            imageListAdapter?.notifyItemRangeInserted(0, it.size)
            setEmptyListView()
        }.apply { compositeDisposable.add(this) }
    }

    private fun setEmptyListView() {
        if (viewModel.imageList.isEmpty())
            binding.tvNoneNotify.visibility = View.VISIBLE
        else
            binding.tvNoneNotify.visibility = View.GONE
    }

    private fun finishRefresh() {
        if (binding.layoutSwipeRefresh.isRefreshing)
            binding.layoutSwipeRefresh.isRefreshing = false
    }

    private fun setProgress(visible: Boolean) {
        binding.progress.isVisible = visible
        if (!visible)
            finishRefresh()
    }
}