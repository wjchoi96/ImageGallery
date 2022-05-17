package com.gallery.kakaogallery.view.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.util.AppHelper

abstract class BaseViewHolder(
    viewDataBinding: ViewDataBinding
) : RecyclerView.ViewHolder(viewDataBinding.root) {
    protected val TAG = AppHelper.getTag(this::class.java)
}