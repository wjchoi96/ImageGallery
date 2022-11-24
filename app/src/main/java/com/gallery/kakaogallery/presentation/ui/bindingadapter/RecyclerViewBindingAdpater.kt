package com.gallery.kakaogallery.presentation.ui.bindingadapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("itemDecoration")
fun addItemDecoration(rv: RecyclerView, itemDecoration: RecyclerView.ItemDecoration){
    rv.addItemDecoration(itemDecoration)
}
