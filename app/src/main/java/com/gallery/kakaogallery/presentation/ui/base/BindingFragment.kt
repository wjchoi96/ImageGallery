package com.gallery.kakaogallery.presentation.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BindingFragment<T : ViewDataBinding> : Fragment() {
    lateinit var vd : T
    abstract val layoutResId : Int

    protected var mContext : Context? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vd = DataBindingUtil.inflate(inflater, layoutResId, container, false)
        return  vd.root as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vd.lifecycleOwner = viewLifecycleOwner
    }

    override fun onDetach() {
        super.onDetach()
        if ( mContext != null )
            mContext = null
    }
}