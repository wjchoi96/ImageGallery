package com.gallery.kakaogallery.presentation.ui.base

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.gallery.kakaogallery.presentation.ui.root.FragmentRootHandler

abstract class BaseFragmentUseHandler<T : ViewDataBinding> : BindingFragment<T>() {

    protected var fHandler: FragmentRootHandler? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentRootHandler)
            fHandler = context
    }

    override fun onDetach() {
        super.onDetach()
        if (fHandler != null)
            fHandler = null
    }
}