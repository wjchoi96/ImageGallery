package com.gallery.kakaogallery.presentation.ui.bindingadapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.gallery.kakaogallery.R

@BindingAdapter("app:loadUrl")
fun loadUrl(imageView: ImageView, url: String) {
    Glide.with(imageView)
        .load(url)
        .error(R.drawable.bg_image_error)
        .placeholder(R.drawable.bg_image_placeholder)
        .override(imageView.layoutParams.width, imageView.layoutParams.height)
        .into(imageView)
}