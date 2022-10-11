package com.gallery.kakaogallery.presentation.ui.base

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.gallery.kakaogallery.presentation.ui.root.FragmentHandler
import com.gallery.kakaogallery.presentation.viewmodel.BaseViewModel

abstract class BaseFragmentUseHandler<T : ViewDataBinding, R : BaseViewModel> : BaseFragment<T, R>() {

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

    override fun startMainFunc() {
        super.startMainFunc()
    }

    override fun bind() {
    }


}