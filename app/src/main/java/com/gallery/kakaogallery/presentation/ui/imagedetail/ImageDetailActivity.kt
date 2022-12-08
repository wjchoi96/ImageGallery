package com.gallery.kakaogallery.presentation.ui.imagedetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.Transition
import android.view.MenuItem
import androidx.activity.viewModels
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.ActivityImageDetailBinding
import com.gallery.kakaogallery.presentation.ui.base.BindingActivity
import com.gallery.kakaogallery.presentation.viewmodel.ImageDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

/*
    https://developer.android.com/develop/ui/views/animations/transitions/start-activity
    https://www.thedroidsonroids.com/blog/how-to-use-shared-element-transition-with-glide-in-4-steps
 */
@AndroidEntryPoint
class ImageDetailActivity : BindingActivity<ActivityImageDetailBinding>() {
    companion object {
        const val VIEW_NAME_IMAGE_DETAIL = "view:name:image:detail"
        fun get(
            context: Context,
            imageUrl: String
        ): Intent = Intent(context, ImageDetailActivity::class.java).apply {
            putExtra(ImageDetailViewModel.EXTRA_IMAGE_DETAIL, imageUrl)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }

    override val layoutResId: Int
        get() = R.layout.activity_image_detail

    private val viewModel: ImageDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpTransition()
        bindView()
        setActionBar()
        observeData()
    }

    private fun setUpTransition() {
        supportPostponeEnterTransition() // Postpone the entering activity transition when Activity was started with
        binding.ivImage.transitionName = VIEW_NAME_IMAGE_DETAIL
        window.sharedElementEnterTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition?) {}

            override fun onTransitionEnd(transition: Transition?) {
                window.sharedElementEnterTransition.removeListener(this)
                viewModel.reloadIfCacheLoadFail()
            }

            override fun onTransitionCancel(transition: Transition?) {}
            override fun onTransitionPause(transition: Transition?) {}
            override fun onTransitionResume(transition: Transition?) {}
        })
    }

    private fun bindView(){
        binding.viewModel = viewModel
    }

    private fun setActionBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }

    private fun observeData() {
        viewModel.uiEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                when (it) {
                    is ImageDetailViewModel.UiEvent.Dismiss -> finishAfterTransition()
                    is ImageDetailViewModel.UiEvent.PostLoadImage -> supportStartPostponedEnterTransition()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}