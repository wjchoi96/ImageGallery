package com.gallery.kakaogallery.view.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.gallery.kakaogallery.util.AppHelper

abstract class BaseRelativeComp<T : ViewDataBinding> constructor(
    context: Context,
    attrs : AttributeSet? = null,
    defStyle : Int = 0,
    defStyleRes : Int = 0
) : RelativeLayout(context, attrs, defStyle, defStyleRes)  {
    protected val TAG = AppHelper.getTag(this::class.java)

    abstract val layoutResId : Int
    lateinit var vd : T

    init {
        init()
    }
    private fun init(){ // setContentView, inflate 의 차이
        //vd = DataBindingUtil.setContentView(context as Activity, layoutResId) // 이건 헤더같은 경우 action bar 에 붙지않고 그 아래 Linear 처럼 쌓이듯이 뷰가 생성된다
        vd = DataBindingUtil.inflate(LayoutInflater.from(context), layoutResId, this, true)
    }
}