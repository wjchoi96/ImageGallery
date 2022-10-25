package com.gallery.kakaogallery.presentation.viewmodel

/**
 * https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
 * liveData 를 observe 하는 순간 liveData 의 가장 최근 value 가 전달되는데,
 * 뷰에 표시되는 data 형식이라면 문제가 없지만 open drawer, show toast 등 사용자에게 직접적으로 전달되는 Event 라면 문제가 생길 수 있다
 * 예) showToast event 가 발생한 이후 화면 회전이 이루어지면, onCreate 부터 코드가 재실행되며 observe 코드까지 다시 실행이 될 것 이고,
 * 그때 showToast event 가 다시 실행된다
 *
 * 화면 회전시 onDestroy 이후 onCreate 가 다시 호출되기 때문에 observe 는 꼭 다시 진행해 주어야 한다.
 */
class SingleEvent<T>(
    private val content: T
){
    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * 한번 사용된 event 는 null 을 리턴
     * 사용하지 않았던 데이터는 사용처리를 진행 후 리턴
     */
    fun getContentIfNotHandled(): T? {
        return if(hasBeenHandled)
            null
        else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * 사용했던 데이터라도 peek
     */
    fun peekContent(): T = content
}