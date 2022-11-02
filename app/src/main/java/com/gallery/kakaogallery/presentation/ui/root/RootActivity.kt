package com.gallery.kakaogallery.presentation.ui.root

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.ActivityRootBinding
import com.gallery.kakaogallery.presentation.ui.base.BindingActivity
import com.gallery.kakaogallery.presentation.ui.gallery.GalleryFragment
import com.gallery.kakaogallery.presentation.ui.searchimage.SearchImageFragment
import com.gallery.kakaogallery.presentation.viewmodel.RootViewModel
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RootActivity : BindingActivity<ActivityRootBinding>() {
    override val layoutResId: Int
        get() = R.layout.activity_root
    private val viewModel: RootViewModel by viewModels()

    private val menuResIdList = listOf(
        R.id.bottom_menu_search_image,
        R.id.bottom_menu_save_image
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBottomNavigationMenu()
        observeData()
    }

    private val navigationItemSelectedListener = NavigationBarView.OnItemSelectedListener {
        return@OnItemSelectedListener viewModel.clickTabEvent(menuResIdList.indexOf(it.itemId)) // 항목을 선택된 항목으로 표시하려면 true, 아니면 false
    }

    private fun setBottomNavigationMenu() {
        binding.bnvRoot.setOnItemSelectedListener(navigationItemSelectedListener)
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