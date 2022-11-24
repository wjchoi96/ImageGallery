package com.gallery.kakaogallery.presentation.ui.base

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding


abstract class BindingActivity<T : ViewDataBinding> : LifeCycleLoggingActivity() {
    lateinit var binding: T
    abstract val layoutResId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutResId)
    }
}