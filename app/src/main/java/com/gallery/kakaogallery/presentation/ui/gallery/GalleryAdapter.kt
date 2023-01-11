package com.gallery.kakaogallery.presentation.ui.gallery

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.domain.model.GalleryImageListTypeModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.presentation.ui.searchimage.GalleryImageItemViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class GalleryAdapter(
    private val imageItemSelectListener: (ImageModel, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class Payload {
        Select
    }

    private var imageList: List<GalleryImageListTypeModel> = emptyList()

    private fun setList(list: List<GalleryImageListTypeModel>) {
        imageList = list
    }

    private fun getDiffRes(newList: List<GalleryImageListTypeModel>): DiffUtil.DiffResult {
        Timber.d("getDiffRes run at ${Thread.currentThread().name}")
        val diffCallback = GalleryImageDiffUtilCallback(
            this.imageList,
            newList,
            Payload.Select
        )
        return DiffUtil.calculateDiff(diffCallback)
    }

    suspend fun updateList(list: List<GalleryImageListTypeModel>) {
        val newList = list.toList()
        Timber.d("diff debug updateList called oldList[${imageList.size}], newList[${newList.size}]")
        withContext(Dispatchers.Default) {
            val diffRes = getDiffRes(newList)
            withContext(Dispatchers.Main) {
                Timber.d("getDiffRes subscribe run at ${Thread.currentThread().name}")
                setList(newList) // must call main thread
                diffRes.dispatchUpdatesTo(this@GalleryAdapter)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (imageList[position]) {
            is GalleryImageListTypeModel.Skeleton -> GalleryImageListTypeModel.ViewType.Skeleton.id
            else -> GalleryImageListTypeModel.ViewType.Image.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            GalleryImageListTypeModel.ViewType.Skeleton.id -> GallerySkeletonViewHolder.from(parent)
            else -> GalleryImageItemViewHolder.from(
                parent,
                imageItemSelectListener
            )
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GalleryImageItemViewHolder ->
                holder.bind((imageList[position] as GalleryImageListTypeModel.Image).image, true)
        }

    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if(payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        for (payload in payloads) {
            when (payload) {
                Payload.Select -> {
                    when (holder) {
                        is GalleryImageItemViewHolder -> {
                            Timber.d("payload Select : $position => $position")
                            holder.bindIsSelect((imageList[position] as GalleryImageListTypeModel.Image).image)
                        }
                    }
                }
            }
        }
    }
}