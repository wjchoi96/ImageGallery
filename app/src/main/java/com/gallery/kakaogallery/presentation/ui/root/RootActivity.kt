package com.gallery.kakaogallery.presentation.ui.root

import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import androidx.activity.viewModels
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.ActivityMainBinding
import com.gallery.kakaogallery.presentation.ui.gallery.SaveImageFragment
import com.gallery.kakaogallery.presentation.ui.base.BaseActivity
import com.gallery.kakaogallery.presentation.ui.comp.HeaderComp
import com.gallery.kakaogallery.presentation.ui.searchimage.SearchImageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

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

class RootActivity : BaseActivity<ActivityMainBinding, RootViewModel>(), FragmentHandler {
    override val layoutResId: Int
        get() = R.layout.activity_main
    override val viewModel: RootViewModel by viewModels()

    private var naviView : BottomNavigationView? = null

    private val menuResIdList = ArrayList<Int>().apply {
        add(R.id.bottom_menu_search_image)
        add(R.id.bottom_menu_save_image)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        savedInstanceState?.let {
//            viewModel.currentPage = it.getInt("currentPage")
//        }
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putInt("currentPage", viewModel.currentPage)
//    }

    override fun getProgress(): ProgressBar? {
        return null
    }

    override fun initData() {

    }

    override fun initView() {
        setBottomNavigationMenu()
        changeFragment(menuResIdList[viewModel.currentPage])
    }

    override fun getHeader(): HeaderComp {
        return HeaderComp(this)
    }

    private val navigationItemSelectedListener = NavigationBarView.OnItemSelectedListener {
        changeFragment(it.itemId)
        return@OnItemSelectedListener true // 항목을 선택된 항목으로 표시하려면 true, 아니면 false
    }
    private fun setBottomNavigationMenu(){
        vd.bottomNavigationBar.setOnItemSelectedListener(navigationItemSelectedListener)
    }

    /*
        1. findFragmentById 을 안쓰고 findFragmentByTag 를 사용한 이유
        2. let 설명
        3. commit, commitAllowingStateLoss 차이
     */
    private fun changeFragment(fragmentResId : Int){
        val transaction = supportFragmentManager.beginTransaction()
        var fragment = supportFragmentManager.findFragmentByTag("$fragmentResId")
        viewModel.currentPage = menuResIdList.indexOf(fragmentResId)
        if(fragment == null){ // 해당 resId fragment 가 없다면 생성해준다
            fragment = when(fragmentResId){
                menuResIdList[0] -> {
                    SearchImageFragment()
                }
                menuResIdList[1] -> {
                    SaveImageFragment()
                }
                else -> return
            }
            transaction.add(R.id.container, fragment, "$fragmentResId")
        }
        Log.d(TAG, "search : ${fragment is SearchImageFragment}, save : ${fragment is SaveImageFragment}")
        transaction.show(fragment)
        for(res in menuResIdList){
            if(res == fragmentResId)
                continue
            supportFragmentManager.findFragmentByTag("$res")?.let {
                transaction.hide(it)
            }
        }
        transaction.commitAllowingStateLoss()
    }


    // 탭 변경시 replace 되는 문제를 해결하기 위해서는 navigation 사용방식이 아닌 직접 fragment 를 컨트롤 해줘야할듯한데
//    private fun setBottomNavigationMenu(){
//        naviView = vd.bottomNavigationBar
////        val naviController = findNavController(R.id.fragmentNaviHost)
//        val naviController = (supportFragmentManager.findFragmentById(R.id.fragmentNaviHost) as NavHostFragment).navController
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                menuResIdList[0],
//                menuResIdList[1]
//            )
//        )
//        setupActionBarWithNavController(naviController, appBarConfiguration)
//        naviView?.setupWithNavController(naviController)
//        naviView?.setOnItemSelectedListener(navigationItemSelectedListener)
//    }
//    private val navigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener {
//        val naviController = findNavController(R.id.fragmentNaviHost)
//        val fragmentId = it.itemId
//        naviController.navigate(fragmentId)
//        return@OnNavigationItemSelectedListener true
//    }

    override fun startMainFunc() {
        super.startMainFunc()
    }

    override fun bind() {
        viewModel.errorMessageObservable.subscribe {
            Log.d(TAG, "error message observable subscribe thread - ${Thread.currentThread().name}")
            setProgress(false)
            showToast(it)
        }.apply { compositeDisposable.add(this) }
    }

    override fun getHeaderCompForChange(): HeaderComp? {
        return headerComp
    }
}