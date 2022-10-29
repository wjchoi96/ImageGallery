package com.gallery.kakaogallery.presentation.ui.searchimage

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.domain.model.ImageListTypeModel
import com.gallery.kakaogallery.domain.model.ImageModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

/**
 * 장기적으로 보면, search comp 를 https://deque.tistory.com/140 처럼 처리해야할것같다
 * 0번째 idx 로 넣는것은 안될듯
 *
 * 검색어가 페이징마다 초기화되는 문제발견
 * 검색창
 * 1. 지금처럼 empty 를 0번째에 넣어서 처리하는게 맞을까
 * 2. list 에는 없지만 getItemCount 조정해서 하는게 맞을까 => x 2-1로 이유 작성
 * 3. 궁극적으로는 빼버려야한다
 *
 * 2-1
 * get Item count +1 을해서 search view holder 를 구현하는 경우, diffUtil 을 통해서 item range insert 될때, 포지션이 올바르게 지정되지않는다
 * 기존 list 의 맨 마지막 item 이 영향을 받고, 동일한 item 이 하나 더 추가가 되는 형식으로 진행된다
 * 추측건데, 10개의 이미지가 있었다면
 * 실제 어댑터는 11개의 이미지로 인지하고 있을것이고
 *
 * 여기에 10개의 이미지가 추가가 된다면
 * diffUtil 은 10개의 oldList(item count 를 가져가는게 아닌 리스트 원본을 가져가서 비교를 한다)
 * 10개의 newList 를 비교하게되고, 10개를 추가해야한다고 결론이 날것
 * 그러면 (10~20) 을 insert 처리를 할텐데, 실제 adapter 에서는 +1 처리가 되어있기때문에 인덱스가 꼬인다
 * !! 결론 ##
 * diffUtil 쓸거면 실제로 없는 데이터를 get Item count 를 수정해서 처리하는 행위는 지양되어야 할것 같다
 */
class SearchImagesAdapter(
    private val searchQueryListener: (String) -> Unit,
    private val queryEditorActionListener: TextView.OnEditorActionListener,
    private val imageItemSelectListener: (ImageModel, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class Payload {
        Query,
        Save,
        Select
    }

    private var imageList: List<ImageListTypeModel> = emptyList()
    val currentItemSize: Int
        get() = imageList.size

    private fun setList(list: List<ImageListTypeModel>) {
        imageList = list
    }

    private fun getDiffRes(newList: List<ImageListTypeModel>): DiffUtil.DiffResult {
        Timber.d("getDiffRes run at ${Thread.currentThread().name}")
        val diffCallback = ImageDiffUtilCallback(
            this.imageList,
            newList,
            Payload.Query,
            Payload.Select,
            Payload.Save
        )
        return DiffUtil.calculateDiff(diffCallback)
    }

    fun updateList(list: List<ImageListTypeModel>) {
        val newList = list.toList()
        Observable.fromCallable{
            getDiffRes(newList)
        }.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("getDiffRes subscribe run at ${Thread.currentThread().name}")
                setList(newList) // must call main thread
                Timber.d("diff debug updateList post : setList[" + this.currentItemSize + "], newList[" + newList.size + "]")
                it.dispatchUpdatesTo(this)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ImageListTypeModel.ViewType.Image.id)
            GalleryImageItemViewHolder.from(parent, imageItemSelectListener)
        else
            SearchQueryViewHolder.from(parent, searchQueryListener, queryEditorActionListener)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0)
            ImageListTypeModel.ViewType.Query.id
        else
            ImageListTypeModel.ViewType.Image.id
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchQueryViewHolder ->
                holder.bind((imageList[position] as ImageListTypeModel.Query))
            is GalleryImageItemViewHolder ->
                holder.bind((imageList[position] as ImageListTypeModel.Image).image)
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
                Payload.Save -> {
                    if (holder is GalleryImageItemViewHolder) {
                        Timber.d("payload Save : $position => $position")
                        holder.bindIsSave((imageList[position] as ImageListTypeModel.Image).image)
                        holder.bindIsSelect((imageList[position] as ImageListTypeModel.Image).image)
                    }
                }
                Payload.Select -> {
                    if (holder is GalleryImageItemViewHolder) {
                        Timber.d("payload Select : $position => $position")
                        holder.bindIsSelect((imageList[position] as ImageListTypeModel.Image).image)
                    }
                }
                Payload.Query -> {
                    if (holder is SearchQueryViewHolder) {
                        holder.bindQuery((imageList[position] as ImageListTypeModel.Query))
                    }
                }
            }
        }
    }
}