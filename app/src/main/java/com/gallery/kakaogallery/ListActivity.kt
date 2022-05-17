package com.gallery.kakaogallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import androidx.activity.viewModels
import com.gallery.kakaogallery.databinding.ActivityListBinding
import com.gallery.kakaogallery.view.base.BaseActivity
import com.gallery.kakaogallery.view.base.BaseViewModel
import com.gallery.kakaogallery.view.comp.HeaderComp
/**
 *  모션 레이아웃 학습
 *  https://developer.android.com/training/constraint-layout/motionlayout/examples?hl=ko
 *  https://medium.com/google-developers/introduction-to-motionlayout-part-iii-47cd64d51a5
 *  https://youngest-programming.tistory.com/353
 *
 *  아직 이해는 안간다
 *
 *  ScrollView 작동 안하던 이유 => layout_behavior 누락( nested scroll view 는 기본적으로 구현 되어있다? )
 *  => ScrollView 는 layout_behavior 추가해도 안된다
 *  https://blog.kmshack.kr/CoordinatorLayout%EA%B3%BC-Behavior%EC%9D%98-%EA%B4%80%EA%B3%84/
 */

/**
 * layout_behavior
 * app:layout_behavior="@string/appbar_scrolling_view_behavior" 속성을 설정함으로써 NestedScrollView의 반응에 따라 AppBarLayout이 반응됩니다.
 * CoordinatorLayout는 NestedScrollView가 스크롤시 layout_behavior에 정의된 레이아웃으로 스크롤 정보를 전달 하는 역할을 합니다. 그럼 AppBarLayout의 ScrollingViewBehavior가 정보를 받아서 AppBarLayout 자신을 변형하도록 하는 구조입니다. (https://blog.kmshack.kr/CoordinatorLayout%EA%B3%BC-Behavior%EC%9D%98-%EA%B4%80%EA%B3%84/)
 *
 * 정말 대애애충 이해한것으론, 해당 설정을 설정해줌으로써 CoordinatorLayout 가 layout_behavior 로 같이 설정된 뷰의 스크롤에 포함된다..인가?
 *
 * scroll content 에만 layout_behavior 해주면 되는거같은데? AppBarLayout 쪽에는 안해도 되는듯
 *
 * app 자체를 noActionBar 로 변경하고,
 * fragment 에 AppBarLayout 을 달아준다?
 * => header comp 를 추가해도 되는거고
 *
 * 아니면 메인만 노 액션바 설정
 */
class ListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
    }
}