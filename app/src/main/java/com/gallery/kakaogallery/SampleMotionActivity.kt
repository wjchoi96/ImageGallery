package com.gallery.kakaogallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

/**
 * layout_scrollFlags
 * - 화면을 스크롤 할때, CollapsingToolbarLayout 을 얼마나 가리고 보일 것인지 정하는 속성
 * - 여러개를 넣을떄는 | 로 구분한다
 *  #https://jhshjs.tistory.com/24 (설명 잘되어있음)
 *  #https://jwsoft91.tistory.com/273 (gif도 포함되어있음)
 *
 *  테스트 환경
 *  scroll|enterAlways|snap|exitUntilCollapsed
 *  이 설정되어있는 상태
 *
 * 0. scroll
 *  : scroll 에 반응하라 ( 사실상 기본으로 넣고 시작하는 속성 )
 *  : 스크롤에 포함되어 이 뷰가 화면에서 사라질 수 있다
 *
 * 1.exitUntilCollapsed
 *  : 위로 스크롤: Toolbar 만 남기고 다 올림
 *  : 아래로 스크롤: 최상단까지 가면, CollapsingToolbarLayout 전체가 내려오기 시작
 *  => 제거해보니, 위로 스크롤시 Toolbar 가 스크롤에 포함되어 올라가 사라진다 ( enterAlways 의 속성이 적용안되다가 적용된다)
 *  => 아래로 스크롤시에는 따로 변경된 부분을 못찾앗다
 *
 * 2. enterAlways
 *  : 위로 스크롤: Toolbar 도 같이 다 올림
 *  : 아래로 스크롤: 스크롤 즉시, CollapsingToolBarLayout 전체가 내려오기 시작
 *  => 제거해보니, 위로 스크롤시 변경점 없음( exitUntilCollapsed 의 속성이 위로 스크롤할때는 우선이 된다고 이해해도 되나 )
 *  => 아래로 스크롤시, 즉시 CollapsingToolBarLayout 가 내려오기 시작(motion 을 시작하던것)하던 현상이 사라지고, 최상단에 도착해서 CollapsingToolBarLayout 이 내려오기 시작한다
 *
 * 3. enterAlwaysCollapsed
 *  : 위로 스크롤: Toolbar 도 같이 올림
 *  : 아래로 스크롤: 최상단까지 가면, CollapsingToolbarLayout 전체가 내려오기 시작
 *  -> 위로는 enterAlways, 아래로는 exitUntilCollapsed 를 적용한 느낌이네
 *
 *  4. snap
 *   : enterAlwaysCollapsed 와 동일한 효과 라고 한다
 *   => 제거했는데, 특별히 달라지는것은 없다
 *
 *   최종적으로는, search view motion 에는 exitUntilCollapsed 만 적용해도 될것같다
 *
 *
 */
class SampleMotionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_motion)
    }
}