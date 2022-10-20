package com.gallery.kakaogallery.presentation.ui.root

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.ActivityRootBinding
import com.gallery.kakaogallery.presentation.ui.base.HeaderCompActivity
import com.gallery.kakaogallery.presentation.ui.comp.HeaderComp
import com.gallery.kakaogallery.presentation.ui.gallery.GalleryFragment
import com.gallery.kakaogallery.presentation.ui.searchimage.SearchImageFragment
import com.gallery.kakaogallery.presentation.viewmodel.RootViewModel
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint

/*
    bottom navigation menu
    1. 직접 fragment transaction 만지기
    https://aries574.tistory.com/149
    2. fragment navigation 사용
    https://developer.android.com/guide/navigation/navigation-getting-started?hl=ko



    bottom navigation + fragment
    show, hide 방식
    https://leveloper.tistory.com/210

    액티비티가 새로 그려지는 이벤트가 발생하면 bottom fragment 와 fragment 가 맞지않는 이슈가 발생할 수 있다

    complete
    1. replace 에서 show, hide 로 바뀌게 되면서 header comp 이슈 해결 ( o )
     - search 에서 이미지 저장시 save 에 바로 반영되도록 설정 ( o )
     - save 에서 이미지 선택중에 새로운 이미지가 추가가 되면 select idx 안꼬이게 처리 ( o )
    2. 페이징 ( o )
    3. 가로모드 지원하자 ( o )
     - main : 현재 페이지 프래그먼트를 기억, 설정 ( o )
     - search : 현재 search 중인 데이터 저장 -> 이벤트 발생시 바로 뷰 설정 ( o )
     - save : search 와 비 ( o )
    5. 네트워크 체크 로직 ( o )
    6. search fragment background touch event 달아서 hide keyboard 하자 ( o )
    8. 스플래시 화면 + 앱 아이콘 + 테마 디자인 ( o )
    9. read me 작성 ( o )
    10. bottom sheet dialog -> 삭제, 저장 ( o )

    포기
    4. 다크모드 ?
    7. 이미지 자세히 보기 화면
    11. 선택모드시 activity 의 bottom tap 을 애니메이션 효과와 함께 내리고, completion 으로 애니메이션이 끝났을때 fragment 에 image menu bottom tap 을 추가?
 */

@AndroidEntryPoint
class RootActivity : HeaderCompActivity<ActivityRootBinding>(), FragmentRootHandler {
    override val layoutResId: Int
        get() = R.layout.activity_root
    private val viewModel: RootViewModel by viewModels()

    private val menuResIdList = ArrayList<Int>().apply {
        add(R.id.bottom_menu_search_image)
        add(R.id.bottom_menu_save_image)
    }

    override fun getHeader(): HeaderComp {
        return HeaderComp(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        setBottomNavigationMenu()
        changeFragment(menuResIdList[viewModel.currentPage])
    }

    private val navigationItemSelectedListener = NavigationBarView.OnItemSelectedListener {
        changeFragment(it.itemId)
        return@OnItemSelectedListener true // 항목을 선택된 항목으로 표시하려면 true, 아니면 false
    }

    private fun setBottomNavigationMenu() {
        binding.bottomNavigationBar.setOnItemSelectedListener(navigationItemSelectedListener)
    }

    /*
        1. findFragmentById 을 안쓰고 findFragmentByTag 를 사용한 이유
        2. let 설명
        3. commit, commitAllowingStateLoss 차이
     */
    private fun changeFragment(fragmentResId: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        var fragment = supportFragmentManager.findFragmentByTag("$fragmentResId")
        viewModel.currentPage = menuResIdList.indexOf(fragmentResId)
        if (fragment == null) { // 해당 resId fragment 가 없다면 생성해준다
            fragment = when (fragmentResId) {
                menuResIdList[0] -> {
                    SearchImageFragment()
                }
                menuResIdList[1] -> {
                    GalleryFragment()
                }
                else -> return
            }
            transaction.add(R.id.container, fragment, "$fragmentResId")
        }
        Log.d("TAG", "search : ${fragment is SearchImageFragment}, save : ${fragment is GalleryFragment}")
        transaction.show(fragment)
        for (res in menuResIdList) {
            if (res == fragmentResId)
                continue
            supportFragmentManager.findFragmentByTag("$res")?.let {
                transaction.hide(it)
            }
        }
        transaction.commitAllowingStateLoss()
    }

    override fun getHeaderCompFromRoot(): HeaderComp? {
        return headerComp
    }
}