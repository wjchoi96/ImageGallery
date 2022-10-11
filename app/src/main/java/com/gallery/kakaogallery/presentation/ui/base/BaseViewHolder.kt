package com.gallery.kakaogallery.presentation.ui.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication

abstract class BaseViewHolder(
    viewDataBinding: ViewDataBinding
) : RecyclerView.ViewHolder(viewDataBinding.root) {
    protected val TAG = KakaoGalleryApplication.getTag(this::class.java)
}