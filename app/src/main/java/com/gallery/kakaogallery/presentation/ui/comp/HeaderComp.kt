package com.gallery.kakaogallery.presentation.ui.comp

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.CompHeaderBinding

class HeaderComp constructor(
    context: Context,
    attrs : AttributeSet? = null,
    defStyle : Int = 0,
    defStyleRes : Int = 0
) : RelativeLayout(context, attrs, defStyle, defStyleRes) {

    constructor(context: Context) : this(context, null, 0, 0)

    private val vd : CompHeaderBinding

    init {
        vd = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.comp_header, this, true)
        setDefaultVisible()
    }

    private fun setDefaultVisible(){
        vd.btnTvLeft.visibility = View.GONE
        vd.btnTvRight.visibility = View.GONE
        vd.tvTitle.visibility = View.GONE
    }
    fun clearView(){
        vd.btnTvLeft.visibility = View.GONE
        vd.btnTvRight.visibility = View.GONE
        vd.tvTitle.visibility = View.GONE
    }

    fun setBackgroundClickListener(listener: () -> Unit){
        vd.background.setOnClickListener {
            listener.invoke()
        }
    }

    fun setTitle(title : String?){
        vd.tvTitle.visibility = View.VISIBLE
        vd.tvTitle.text = title
    }

    fun setRightBtnListener(text : String, listener: () -> Unit){
        vd.btnTvRight.visibility = View.VISIBLE
        vd.btnTvRight.text = text
        vd.btnTvRight.setOnClickListener {
            listener.invoke()
        }
    }
    fun removeRightBtn(){
        vd.btnTvRight.visibility = View.GONE
    }

    fun setLeftBtnListener(text: String, listener: () -> Unit){
        vd.btnTvLeft.visibility = View.VISIBLE
        vd.btnTvLeft.text = text
        vd.btnTvLeft.setOnClickListener {
            listener.invoke()
        }
    }
    fun removeLeftBtn(){
        vd.btnTvLeft.visibility = View.GONE
    }
}