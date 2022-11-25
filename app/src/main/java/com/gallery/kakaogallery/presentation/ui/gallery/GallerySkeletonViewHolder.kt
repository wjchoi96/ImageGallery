package com.gallery.kakaogallery.presentation.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.databinding.ItemGalleryImageLoadingBinding

class GallerySkeletonViewHolder private constructor(
    binding: ItemGalleryImageLoadingBinding
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun from(
            parent: ViewGroup
        ): GallerySkeletonViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemGalleryImageLoadingBinding.inflate(layoutInflater, parent, false)
            return GallerySkeletonViewHolder(binding)
        }
    }
}