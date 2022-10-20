package com.gallery.kakaogallery.presentation.ui.base

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.gallery.kakaogallery.presentation.ui.root.FragmentHandler

abstract class BaseFragmentUseHandler<T : ViewDataBinding> : BindingFragment<T>() {

    protected var fHandler : FragmentHandler? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if ( context is FragmentHandler)
            fHandler = context
    }
    override fun onDetach() {
        super.onDetach()
        if( fHandler != null )
            fHandler = null
    }
}