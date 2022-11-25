package com.gallery.kakaogallery.presentation.ui.gallery

import androidx.recyclerview.widget.DiffUtil
import com.gallery.kakaogallery.domain.model.GalleryImageListTypeModel
import timber.log.Timber

class GalleryImageDiffUtilCallback(
    private val oldList: List<GalleryImageListTypeModel>,
    private val newList: List<GalleryImageListTypeModel>,
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
            old is GalleryImageListTypeModel.Image && new is GalleryImageListTypeModel.Image -> {
                when {
                    old.image.isSelect != new.image.isSelect -> selectPayload
                    else -> super.getChangePayload(oldItemPosition, newItemPosition)
                }
            }
            else -> super.getChangePayload(oldItemPosition, newItemPosition)
        }
    }
}