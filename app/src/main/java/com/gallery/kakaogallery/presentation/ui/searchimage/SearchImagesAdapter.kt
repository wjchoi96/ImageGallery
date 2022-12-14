package com.gallery.kakaogallery.presentation.ui.searchimage

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.domain.model.SearchImageListTypeModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.presentation.ui.gallery.GallerySkeletonViewHolder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class SearchImagesAdapter(
    private val searchQueryListener: (String) -> Unit,
    private val queryEditorActionListener: TextView.OnEditorActionListener,
    private val imageItemSelectListener: (ImageModel, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class Payload {
        Query,
        Select
    }

    private var imageList: List<SearchImageListTypeModel> = emptyList()

    private fun setList(list: List<SearchImageListTypeModel>) {
        imageList = list
    }

    private fun getDiffRes(newList: List<SearchImageListTypeModel>): DiffUtil.DiffResult {
        Timber.d("getDiffRes run at ${Thread.currentThread().name}")
        val diffCallback = SearchImageDiffUtilCallback(
            this.imageList,
            newList,
            Payload.Query,
            Payload.Select
        )
        return DiffUtil.calculateDiff(diffCallback)
    }

    suspend fun updateList(list: List<SearchImageListTypeModel>) {
        val newList = list.toList()
        withContext(Dispatchers.Default) {
            val diffRes = getDiffRes(newList)
            withContext(Dispatchers.Main) {
                Timber.d("getDiffRes subscribe run at ${Thread.currentThread().name}")
                setList(newList) // must call main thread
                diffRes.dispatchUpdatesTo(this@SearchImagesAdapter)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SearchImageListTypeModel.ViewType.Query.id -> SearchQueryViewHolder.from(parent, searchQueryListener, queryEditorActionListener)
            SearchImageListTypeModel.ViewType.Skeleton.id -> GallerySkeletonViewHolder.from(parent)
            else -> GalleryImageItemViewHolder.from(parent, imageItemSelectListener)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (imageList[position]) {
            is SearchImageListTypeModel.Query -> SearchImageListTypeModel.ViewType.Query.id
            is SearchImageListTypeModel.Skeleton -> SearchImageListTypeModel.ViewType.Skeleton.id
            else -> SearchImageListTypeModel.ViewType.Image.id
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchQueryViewHolder ->
                holder.bind((imageList[position] as SearchImageListTypeModel.Query))
            is GalleryImageItemViewHolder ->
                holder.bind((imageList[position] as SearchImageListTypeModel.Image).image, false)
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
                    if (holder is GalleryImageItemViewHolder) {
                        Timber.d("payload Select : $position => $position")
                        holder.bindIsSelect((imageList[position] as SearchImageListTypeModel.Image).image)
                    }
                }
                Payload.Query -> {
                    if (holder is SearchQueryViewHolder) {
                        holder.bindQuery((imageList[position] as SearchImageListTypeModel.Query))
                    }
                }
            }
        }
    }
}