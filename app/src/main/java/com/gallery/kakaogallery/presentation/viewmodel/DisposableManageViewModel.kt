package com.gallery.kakaogallery.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject

/*
    https://developer.android.com/topic/libraries/architecture/viewmodel?hl=ko
    by viewModels() 를 사용해 전권을 위임해 사용한다 => View model provider 사용 대체

    https://medium.com/androiddevelopers/viewmodels-a-simple-example-ed5ac416317e
    view model 간단한 설명

    https://jupiny.com/2020/04/11/rxjava-subscribeon-observeon-1/
    subscribeOn => upstream 의 Subscription 의 reqeust 를 실행할 scheduler 설정
    observeOn => downstream Subscriber 동작(onNext ~ )을 실행할 scheduler 설정
 */
/**
 * live data for event
 * event 들은 single live data 로 처리되어야 할것이다
 * => live data 를 data 와 event 로 분류하는 개념
 * https://vagabond95.me/posts/live-data-with-event-issue/
 * => 테드박님
 * https://medium.com/prnd/mvvm%EC%9D%98-viewmodel%EC%97%90%EC%84%9C-%EC%9D%B4%EB%B2%A4%ED%8A%B8%EB%A5%BC-%EC%B2%98%EB%A6%AC%ED%95%98%EB%8A%94-%EB%B0%A9%EB%B2%95-6%EA%B0%80%EC%A7%80-31bb183a88ce
 *
 * start activity event
 * 어떤 Activity가 열리는지 결정이 View에 있다면, 그것에 대한 JUnit 테스트를 작성하는 것은 매우 어렵습니다
 * https://stackoverflow.com/a/51256998
 *
 * data
 * - view 의 상태, view 를 구성하는 데이터
 * 예) visible, list, item model
 * 해당 유형의 live data 들은 live data 의 중복실행 문제에 치명적이지 않다
 * => 하지만 중복실행 자체는 의도된게 아니니 single live data 사용 + 뷰를 재구성이 필요할때는 view model 에 알려서 처리하도록 변경 고려
 *
 * event
 * -fragment, activity 단에서 처리되어야 하는 이벤트
 * 예) start activity, show toast, hide keyboard, show snack bar 등
 * 해당 유형의 live data 들은 live data 의 중복실행 issue 발생에 치명적이다
 * => single live data 사용 방식 고려
 */
abstract class DisposableManageViewModel : ViewModel() {
    protected val compositeDisposable: CompositeDisposable = CompositeDisposable()

    // activity onDestroy 뒤에 호출
    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}