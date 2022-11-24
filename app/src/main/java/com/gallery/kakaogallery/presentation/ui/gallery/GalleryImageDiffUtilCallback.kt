package com.gallery.kakaogallery.presentation.ui.gallery

import androidx.recyclerview.widget.DiffUtil
import com.gallery.kakaogallery.domain.model.GalleryImageModel
import timber.log.Timber

class GalleryImageDiffUtilCallback(
    private val oldList: List<GalleryImageModel>,
    private val newList: List<GalleryImageModel>,
    private val selectPayload: Any?
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] isSameItem newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] isSameContent newList[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        Timber.d("getChangePayload called")
        return when {
            old.isSelect != new.isSelect -> selectPayload
            else -> super.getChangePayload(oldItemPosition, newItemPosition)
        }
    }
}