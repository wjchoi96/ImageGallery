package com.gallery.kakaogallery.presentation.ui.searchimage

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.databinding.ItemGalleryImageBinding
import com.gallery.kakaogallery.domain.model.ImageModel
import timber.log.Timber

@SuppressLint("ClickableViewAccessibility")
class GalleryImageItemViewHolder private constructor(
    private val binding: ItemGalleryImageBinding,
    itemSelectListener: (ImageModel, Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(
            parent: ViewGroup,
            imageItemSelectListener: (ImageModel, Int) -> Unit
        ): GalleryImageItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemGalleryImageBinding.inflate(layoutInflater, parent, false)
            return GalleryImageItemViewHolder(binding, imageItemSelectListener)
        }
    }

    val itemPosition: Int
        get() = bindingAdapterPosition

    val imageItemSelectListener: (ImageModel, Int) -> Unit by lazy {
        { image, idx ->
            Timber.d("animation debug => click")
            scaleAnimate(binding.cvImage, 1f) {
                itemSelectListener.invoke(image, idx)
            }
        }
    }

    init {
        binding.background.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    Timber.d("animation debug => ACTION_DOWN")
                    scaleAnimate(binding.cvImage, 0.93f)
                }

                MotionEvent.ACTION_UP -> {
                    Timber.d("animation debug => ACTION_UP")
                    when (motionEvent.x !in 0f..view.width.toFloat() || motionEvent.y !in 0f..view.height.toFloat()) {
                        true -> {
                            scaleAnimate(binding.cvImage, 1f)
                        }
                        else -> {}
                    }
                }

                MotionEvent.ACTION_CANCEL -> {
                    Timber.d("animation debug => ACTION_CANCEL")
                    scaleAnimate(binding.cvImage, 1f)
                }
            }
            false
        }
    }

    fun bind(item: ImageModel, isSave: Boolean) {
        binding.holder = this
        binding.imageItem = item
        bindIsSelect(item)
        bindIsSave(isSave)
    }

    fun bindIsSelect(item: ImageModel) {
        binding.isSelectImage = item.isSelect
    }

    private fun bindIsSave(isSave: Boolean) {
        binding.isSaveImage = isSave
    }

    private fun scaleAnimate(view: View, scale: Float, duration: Long = 100, endAction: ((View) -> Unit)? = null ) {
        view.animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(duration)
            .withEndAction {
                endAction?.invoke(view)
            }
    }

}