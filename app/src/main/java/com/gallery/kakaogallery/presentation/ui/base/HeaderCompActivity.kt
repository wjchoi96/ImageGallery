package com.gallery.kakaogallery.presentation.ui.base

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import com.gallery.kakaogallery.presentation.ui.comp.HeaderComp

abstract class HeaderCompActivity<T : ViewDataBinding>: BindingActivity<T>() {
    protected var headerComp : HeaderComp? = null
    abstract fun getHeader() : HeaderComp?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBar()
    }

    private fun setBar(){
        val actionBar  = supportActionBar!!

        actionBar.setDisplayShowCustomEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(false)
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.elevation = 0f

        headerComp = getHeader()
        if(headerComp == null){
            actionBar.hide()
            return
        }else
            actionBar.show()

        actionBar.customView = headerComp

        //val tool : Toolbar = actionBarView.parent as Toolbar
        val tool : Toolbar = headerComp?.parent as Toolbar
        tool.setContentInsetsAbsolute(0,0)

        val params : ActionBar.LayoutParams = ActionBar.LayoutParams(
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.MATCH_PARENT,
            Gravity.CENTER)

        //actionBar.setCustomView(actionBarView,params)
        actionBar.setCustomView(headerComp ,params)
    }
}