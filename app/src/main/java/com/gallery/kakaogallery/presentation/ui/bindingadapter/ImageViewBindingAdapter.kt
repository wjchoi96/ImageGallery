package com.gallery.kakaogallery.presentation.ui.bindingadapter

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.gallery.kakaogallery.R
import timber.log.Timber

@BindingAdapter("app:loadUrl")
fun loadUrl(imageView: ImageView, url: String) {
    Glide.with(imageView)
        .load(url)
        .error(R.drawable.bg_image_error)
        .placeholder(R.drawable.bg_image_placeholder)
        .override(imageView.layoutParams.width, imageView.layoutParams.height)
        .into(imageView)
}

/**
 * loadInfo.first => url
 * loadInfo.second => loadFromCache
 */
@BindingAdapter("app:loadUrl", "app:loadOnlyCache", "app:loadUrlFinishListener")
fun loadUrlWithListener(imageView: ImageView, imageUrl: String, loadOnlyCache: Boolean, onLoadingFinish: (Boolean) -> Unit) {
    Timber.d("animation debug => loadUrlWithListener\nurl[$imageUrl], loadOnlyCache[$loadOnlyCache]")
    Glide.with(imageView)
        .load(imageUrl)
        .error(if(loadOnlyCache) R.drawable.bg_image_placeholder else R.drawable.bg_image_error)
        .dontTransform()
        .onlyRetrieveFromCache(loadOnlyCache)
        .placeholder(R.drawable.bg_image_placeholder)
        .override(imageView.layoutParams.width, imageView.layoutParams.height)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                onLoadingFinish(false)
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onLoadingFinish(true)
                return false
            }
        })
        .into(imageView)
}