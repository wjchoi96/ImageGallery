package com.gallery.kakaogallery.presentation.ui.searchimage

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.databinding.ItemSearchImageBinding
import com.gallery.kakaogallery.databinding.ItemSearchQueryBinding
import com.gallery.kakaogallery.model.ImageModel
import com.gallery.kakaogallery.presentation.ui.base.BaseViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
/*
    이건 좀 당장은 아닌거같다
    기존 방식 + LiveData + view model + 코루틴 + repository 로 변경하는거로 노선 틀자
    LiveData 만 6단계 우선 진행해보자
 */

class SearchImagesListAdapter(
    private val viewModel : SearchImageViewModel
): ListAdapter<SearchImageAdapterData, RecyclerView.ViewHolder>(ImageDiffCallback()) {
    companion object {
        const val SearchQueryType = 0
        const val ImageType = 1
    }
    private val adapterScope = CoroutineScope(Dispatchers.Default)

    //https://chachas.tistory.com/46
    fun addHeaderAndSubmit(list : List<ImageModel>?, completion : (()->(Unit))? = null){
        Log.d("test", "addHeaderAndSubmit : ${list?.size}")
        adapterScope.launch {
            val items = when(list){
                null -> listOf(SearchImageAdapterData.Header)
                else -> listOf(SearchImageAdapterData.Header) + list.map {
                    SearchImageAdapterData.SearchImageData(
                        it
                    )
                }
            }
            withContext(Dispatchers.Main){
                submitList(items) {
                    completion?.invoke()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return currentList[position].id
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ImageType)
            ImageViewHolder.from(parent)
        else
            QueryViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("test", "onBindViewHolder[$position] run on thread : ${Thread.currentThread().name}")
        if(holder is ImageViewHolder) {
            holder.bind(viewModel, (getItem(position) as SearchImageAdapterData.SearchImageData).imageModel)
        }else if(holder is QueryViewHolder)
            holder.bind(viewModel)
    }

    class ImageViewHolder private constructor(
        private val vd : ItemSearchImageBinding
    ): BaseViewHolder(vd) {
        companion object {
            fun from(parent : ViewGroup) : ImageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSearchImageBinding.inflate(layoutInflater, parent, false)
                return ImageViewHolder(binding)
            }
        }

        private val itemPosition : Int
            get() = adapterPosition

        fun bind(viewModel: SearchImageViewModel, item : ImageModel){
            vd.viewModel = viewModel
            vd.item = item
            vd.position = itemPosition
            vd.executePendingBindings()
        }

    }

    class QueryViewHolder private constructor(
        private val vd : ItemSearchQueryBinding
    ) : BaseViewHolder(vd){
        companion object {
            fun from(parent : ViewGroup) : QueryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSearchQueryBinding.inflate(layoutInflater, parent, false)
                return QueryViewHolder(binding)
            }
        }
        val query : String
            get() = vd.etQuery.text.toString()

        fun bind(viewModel: SearchImageViewModel){
            vd.viewModel = viewModel
//            vd.query = query
            vd.executePendingBindings()
        }
    }
}

/*
    sealed class 란 child 로 가질 수 있는 class 에 제한이 있는 클래스
    정의된 하위 클래스 이외의 하위 클래스는 가질 수 없다
    마치 enum 의 특성과 비슷하다
 */
sealed class SearchImageAdapterData {
    data class SearchImageData(val imageModel : ImageModel) : SearchImageAdapterData(){
        override val id: Int = SearchImagesListAdapter.ImageType
    }
    object Header: SearchImageAdapterData() {
        override val id: Int = SearchImagesListAdapter.SearchQueryType
    }
    abstract val id: Int
}

class ImageDiffCallback : DiffUtil.ItemCallback<SearchImageAdapterData>() {
    override fun areItemsTheSame(oldItem: SearchImageAdapterData, newItem: SearchImageAdapterData): Boolean {
        Log.d("test", "areItemsTheSame run on thread : ${Thread.currentThread().name}\n")
        return if(oldItem is SearchImageAdapterData.SearchImageData && newItem is SearchImageAdapterData.SearchImageData)
            oldItem.imageModel.imageThumbUrl == newItem.imageModel.imageThumbUrl
        else
            true
    }

    override fun areContentsTheSame(oldItem: SearchImageAdapterData, newItem: SearchImageAdapterData): Boolean {
        Log.d("test", "areContentsTheSame run on thread : ${Thread.currentThread().name}")
        return if(oldItem is SearchImageAdapterData.SearchImageData && newItem is SearchImageAdapterData.SearchImageData)
            oldItem == newItem
        else true
    }
}