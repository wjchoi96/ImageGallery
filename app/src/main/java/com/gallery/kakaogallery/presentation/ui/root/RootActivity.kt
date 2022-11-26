package com.gallery.kakaogallery.presentation.ui.root

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.ActivityRootBinding
import com.gallery.kakaogallery.presentation.ui.base.BindingActivity
import com.gallery.kakaogallery.presentation.ui.gallery.GalleryFragment
import com.gallery.kakaogallery.presentation.ui.searchimage.SearchImageFragment
import com.gallery.kakaogallery.presentation.viewmodel.RootViewModel
import com.gallery.kakaogallery.presentation.viewmodel.SplashViewModel
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RootActivity : BindingActivity<ActivityRootBinding>() {
    companion object {
        private var splashNeedShow = true
    }
    override val layoutResId: Int
        get() = R.layout.activity_root
    private val viewModel: RootViewModel by viewModels()
    private val splashViewModel: SplashViewModel by viewModels()

    private val menuResIdList = listOf(
        R.id.bottom_menu_search_image,
        R.id.bottom_menu_save_image
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = initSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen?.let { setUpSplashScreen(it) }
        setBottomNavigationMenu()
        observeData()
    }

    private fun initSplashScreen(): SplashScreen? {
        return when (splashNeedShow) {
            true -> installSplashScreen()
            else -> {
                setTheme(R.style.Theme_KakaoGallery)
                null
            }
        }
    }

    private fun setUpSplashScreen(splashScreen: SplashScreen){
        splashNeedShow = false
        binding.root.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (splashViewModel.isReady) {
                        binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            }
        )
        splashScreen.setOnExitAnimationListener { splashView ->
            AnimatorSet().apply {
                playSequentially(
                    listOf(
                        ObjectAnimator.ofFloat(splashView.iconView, View.ROTATION, 0f, -10f).setDuration(50),
                        ObjectAnimator.ofFloat(splashView.iconView, View.ROTATION, -10f, 10f).setDuration(50),
                        ObjectAnimator.ofFloat(splashView.iconView, View.ROTATION, 10f, -10f).setDuration(50),
                        ObjectAnimator.ofFloat(splashView.iconView, View.ROTATION, -10f, 0f).setDuration(50),
                        ObjectAnimator.ofFloat(splashView.iconView, View.ROTATION, 0f, -10f).setDuration(50),
                        ObjectAnimator.ofFloat(splashView.iconView, View.ROTATION, 0f, 0f).setDuration(50),
                        ObjectAnimator.ofFloat(splashView.iconView, View.ALPHA, 0.0f).setDuration(500)
                    )
                )
                doOnEnd {
                    splashView.remove()
                }
            }.start()
        }
    }

    private fun setBottomNavigationMenu() {
        binding.bnvRoot.setOnItemSelectedListener(navigationItemSelectedListener)
    }
    private val navigationItemSelectedListener = NavigationBarView.OnItemSelectedListener {
        return@OnItemSelectedListener viewModel.clickTabEvent(menuResIdList.indexOf(it.itemId)) // 항목을 선택된 항목으로 표시하려면 true, 아니면 false
    }

    private fun observeData(){
        viewModel.currentPage.observe(this){
            changeFragment(it)
        }
    }

    private fun changeFragment(tabIdx: Int) {
        val tag = "$tabIdx"
        val transaction = supportFragmentManager.beginTransaction()
        val fragment: Fragment = supportFragmentManager.findFragmentByTag(tag) ?: kotlin.run { // 해당 tag fragment 가 없다면 생성해준다
             when (tabIdx) {
                0 -> SearchImageFragment()
                1 -> GalleryFragment()
                else -> return
            }.also {
                 transaction.add(R.id.layout_container, it, tag)
            }
        }
        transaction.show(fragment)
        for (idx in menuResIdList.indices) {
            if(idx == tabIdx) continue
            val currentFragmentTag = "$idx"
            supportFragmentManager.findFragmentByTag(currentFragmentTag)?.let {
                transaction.hide(it)
            }
        }
        transaction.commitAllowingStateLoss() // commit 과 차이점?
    }
}