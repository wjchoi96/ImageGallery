package com.gallery.kakaogallery.presentation.ui.imagedetail

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.view.*
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
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
        }
    }

    override val layoutResId: Int
        get() = R.layout.activity_image_detail

    private val viewModel: ImageDetailViewModel by viewModels()

    private val fadeOut: ObjectAnimator by lazy { ObjectAnimator.ofFloat(binding.layoutAppBar, View.ALPHA, 1f, 0f) }
    private val fadeIn: ObjectAnimator by lazy { ObjectAnimator.ofFloat(binding.layoutAppBar, View.ALPHA, 0f, 1f) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpTransition()
        bindView()
        setActionBar()
        setListener()
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        setSupportActionBar(binding.toolBar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        setTopMarginAsStatusBar()
    }

    // https://developer.android.com/develop/ui/views/layout/edge-to-edge#handle-overlaps
    private fun setTopMarginAsStatusBar() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutAppBar) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setListener() {
        binding.ivImage.setOnDoubleTapListener(object : GestureDetector.OnDoubleTapListener {
            override fun onSingleTapConfirmed(p0: MotionEvent): Boolean {
                viewModel.touchBackgroundEvent()
                return false
            }

            override fun onDoubleTap(p0: MotionEvent): Boolean = false

            override fun onDoubleTapEvent(p0: MotionEvent): Boolean = false
        })
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

        viewModel.fullScreenMode.observe(this) {
            when (it) {
                true -> setFadeOutAnimation(animationStartListener = {
                    hideSystemBar()
                })
                else -> setFadeInAnimation(animationStartListener = {
                    showSystemBar()
                })
            }
        }
    }


    private fun showSystemBar(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.show(WindowInsets.Type.systemBars())
        } else@Suppress("DEPRECATION") {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    private fun hideSystemBar(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else@Suppress("DEPRECATION") {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE or
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    // Hide the nav bar and status bar
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        }
    }

    private fun setFadeOutAnimation(duration: Long = 200, animationStartListener: (() -> Unit)? = null, animationEndListener: (() -> Unit)? = null) {
        fadeOut.apply {
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {
                    animationStartListener?.invoke()
                }
                override fun onAnimationEnd(p0: Animator) {
                    animationEndListener?.invoke()
                }
                override fun onAnimationCancel(p0: Animator) {
                    animationEndListener?.invoke()
                }
                override fun onAnimationRepeat(p0: Animator) {}
            })
            this.duration = duration
        }
        if(fadeIn.isRunning)
            fadeIn.cancel()
        fadeOut.start()
    }

    private fun setFadeInAnimation(duration: Long = 200, animationStartListener: (() -> Unit)? = null, animationEndListener: (() -> Unit)? = null) {
        fadeIn.apply {
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {
                    animationStartListener?.invoke()
                }
                override fun onAnimationEnd(p0: Animator) {
                    animationEndListener?.invoke()
                }
                override fun onAnimationCancel(p0: Animator) {
                    animationEndListener?.invoke()
                }
                override fun onAnimationRepeat(p0: Animator) {}
            })
            this.duration = duration
        }
        if(fadeOut.isRunning)
            fadeOut.cancel()
        fadeIn.start()
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