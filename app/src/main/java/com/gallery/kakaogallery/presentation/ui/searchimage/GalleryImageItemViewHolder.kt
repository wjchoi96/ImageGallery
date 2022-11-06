package com.gallery.kakaogallery.presentation.ui.searchimage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.databinding.ItemGalleryImageBinding
import com.gallery.kakaogallery.domain.model.ImageModel

class GalleryImageItemViewHolder private constructor(
    private val binding: ItemGalleryImageBinding,
    val imageItemSelectListener: (ImageModel, Int) -> Unit
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

}