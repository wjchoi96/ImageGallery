package com.gallery.kakaogallery.presentation.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding


abstract  class BindingActivity<T : ViewDataBinding> : AppCompatActivity() {
    lateinit var vd : T
    abstract val layoutResId : Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vd = DataBindingUtil.setContentView(this, layoutResId)
    }

}