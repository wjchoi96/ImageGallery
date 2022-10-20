package com.gallery.kakaogallery.presentation.ui.searchimage

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.ItemSearchImageBinding
import com.gallery.kakaogallery.databinding.ItemSearchQueryBinding
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication
import com.gallery.kakaogallery.presentation.viewmodel.SearchImageViewModel
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
    private val viewModel: SearchImageViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val SearchQueryType = 0
        const val ImageType = 1
    }

    enum class ImagePayload {
        Save,
        Select
    }

    enum class SearchPayload {
        Query
    }

    val currentItemSize: Int
        get() = imageList.size

    private var lastQuery: String? = null
    private var imageList: List<ImageModel> = emptyList()
    fun setList(list: List<ImageModel>) {
        val newList = mutableListOf<ImageModel>()
        newList.addAll(list)
        if (newList.firstOrNull() != ImageModel.Empty) {
            Timber.d("diff debug set list add empty")
            newList.add(0, ImageModel.Empty)
        }
        imageList = newList
    }

    fun setLastQuery(lastQuery: String?) {
        this.lastQuery = lastQuery
        notifyItemChanged(0, SearchPayload.Query)// payload 를 통해 검색어만 바꾸자
    }

    /**
     * old list 가 비어있을때 호출되면 crash 가 나는것 같다
     * 근데 아깐 안비어있게 해놨는데
     * search 뷰 부분까지도 changed 에 포함되는게 맘에 안드네
     * search 뷰 부분을 실제로 data 를 부여하던지,
     *
     * dispatchUpdatesTo IndexOutOfBoundsException 이슈 지속된다
     * => https://medium.com/@mohammad.yahia.jad/the-order-in-which-dispatchupdates-works-679e589fb54e
     * => setList, dispatchUpdatesTo 하지말고 dispatchUpdatesTo, setList 해봐라
     * => 순서를 바꾸니까 문제없이 잘된다. 근데 왜 이러는건지 알아봐야할듯
     * dispatchUpdatesTo 동작 방식좀 파보자
     */
    /**
     * dispatchUpdatesTo 문제가 아니라
     * 백그라운드 쓰레드에서 adapter list 를 업데이트 후, notifyItemRangeInserted 를 호출하면 문제가 발생하는것
     * - (DiffUtil 을 사용해서건, 사용안하건 똑같았다, ListUpdateCallback 까지 사용해서 확인해봄)
     * - (NotifyDataSetChanged 는 문제 없었다)
     * - Adapter 에 list 를 set 할때 깊은복사를 권장하고, 접근하는것 또한 메인쓰레드에서 하는것을 권장
     *
     * 이해가 안가서 이것저것 실험을 해보고, 찾아보았지만 원인은 알 수 없었고, 일단 setList 와 notifyAdapter 작업은 메인쓰레드에서만 하는것을 권장해야겠다
     * 원인을 찾아보기 위해 검색을 많이 시도해봤지만, 간간히 나와 같은 결론을 내린 사람은 발견했지만, 원인은 찾을 수 없었다
     *
     * 올바르게 DiffUtil 을 사용하는 방법
     * https://jonfhancock.com/get-threading-right-with-diffutil-423378e126d2
     * 1. 백그라운드에서 Diff 처리
     * 2. 메인쓰레드로 돌아가기
     * 3. 데이터 업데이트, 어댑터에 변경사항을 알리기
     *
     * 문제점: 현재 diff 를 처리하는동안 list 가 변경되는경우
     * 1. 기존데이터 우선: loading 변수를 통해 diff 처리 중 들어오는 최신 데이터 무시
     * 2. 최신데이터 우선: loading 중에 들어오는 새로운 list 를 저장해뒀다가(계속 들어온다면 가장 최신 list 로 갱신),
     *  현재 diff 가 완료되면 해당 diff 를 적용하고 대기중인 list 의 diff 작업 실행
     * 3. 대기열에 넣고 순서대로 적용
     *
     * 중요한점은 diff 를 계산하는것이 순차적이여야한다는것?
     */
    fun updateList(list: List<ImageModel>) {
        if (imageList.isEmpty()) {
            Timber.d("diff debug old list is empty")
            this.setList(list)
            notifyDataSetChanged()
            return
        }
        val newList = list.toMutableList().apply {
            add(0, ImageModel.Empty)
        }
        val handler = Handler(Looper.getMainLooper())
        Thread {
            Timber.d("diff debug updateList thread start")
            val diffCallback = ImageDiffUtilCallback(this.imageList, newList)
            val diffRes = DiffUtil.calculateDiff(diffCallback)
            handler.post {
                this.setList(newList) // must call main thread
                Timber.d("diff debug updateList post : setList[" + this.currentItemSize + "], newList[" + newList.size + "]")
                diffRes.dispatchUpdatesTo(this)
            }
        }.start()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ImageType)
            ImageItemViewHolder.from(parent)
        else
            SearchQueryViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0)
            SearchQueryType
        else
            ImageType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SearchQueryViewHolder) {
            if (!lastQuery.isNullOrBlank() && holder.query == lastQuery) return // 이래도 되나?
            holder.bind(viewModel, lastQuery)
        } else if (holder is ImageItemViewHolder) {
            holder.bind(viewModel, imageList[position])
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        for (payload in payloads) {
            when (payload) {
                ImagePayload.Save -> {
                    if (holder is ImageItemViewHolder) {
                        Timber.d("payload Save : $position => $position")
                        holder.setSaveIcon(imageList[position].isSaveImage)
                        holder.setSelectEffect(imageList[position].isSelect)
                    }
                }
                ImagePayload.Select -> {
                    if (holder is ImageItemViewHolder) {
                        Timber.d("payload Select : $position => $position")
                        holder.setSelectEffect(imageList[position].isSelect)
                    }
                }
                SearchPayload.Query -> {
                    if (holder is SearchQueryViewHolder) {
                        holder.setQuery(lastQuery)
                    }
                }
            }
        }
    }

    class ImageItemViewHolder(
        private val binding: ItemSearchImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ImageItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSearchImageBinding.inflate(layoutInflater, parent, false)
                return ImageItemViewHolder(binding)
            }
        }

        private val itemPosition: Int
            get() = adapterPosition

        fun bind(viewModel: SearchImageViewModel, item: ImageModel) {
            binding.viewModel = viewModel
            binding.item = item
            binding.position = itemPosition
            binding.executePendingBindings()
        }

        fun setSelectEffect(show: Boolean) {
            if (show) {
                binding.background.setBackgroundResource(R.drawable.bg_select_image)
            } else {
                binding.background.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }
        }

        fun setSaveIcon(isSave: Boolean) {
            if (isSave)
                binding.ivStar.visibility = View.VISIBLE
            else
                binding.ivStar.visibility = View.GONE
        }


    }

    class SearchQueryViewHolder(
        private val binding: ItemSearchQueryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): SearchQueryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSearchQueryBinding.inflate(layoutInflater, parent, false)
                return SearchQueryViewHolder(binding)
            }
        }

        val query: String
            get() {
                Timber.d("getQuery : " + binding.etQuery.text)
                return binding.etQuery.text.toString()
            }

        fun bind(viewModel: SearchImageViewModel, lastQuery: String?) {
            setQuery(lastQuery)

            binding.viewModel = viewModel
            binding.holder = this
            binding.executePendingBindings()
        }

        fun setQuery(query: String?) {
            binding.etQuery.setText(query)
        }
    }
}