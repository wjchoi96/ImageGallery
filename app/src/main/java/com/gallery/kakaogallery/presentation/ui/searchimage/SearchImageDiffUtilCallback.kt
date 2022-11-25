package com.gallery.kakaogallery.presentation.ui.searchimage

import androidx.recyclerview.widget.DiffUtil
import com.gallery.kakaogallery.domain.model.SearchImageListTypeModel
import timber.log.Timber

/**
 * https://sohee1702.tistory.com/409
 *
 * 공강 : O(N)
 * 시간 : O(N + D^2)
 * => old/new 두 리스트의 합인 N개 / old가 new로 변환되기 위해 필요한 최소 작업갯수(==edit script) D
 * 최대 사이즈는 2^26(67,108,864)개까지 지원
 */
class SearchImageDiffUtilCallback(
    private val oldList: List<SearchImageListTypeModel>,
    private val newList: List<SearchImageListTypeModel>,
    private val queryPayload: Any?,
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
            old is SearchImageListTypeModel.Query && new is SearchImageListTypeModel.Query -> {
                if (old.query != new.query) {
                    queryPayload
                } else {
                    super.getChangePayload(oldItemPosition, newItemPosition)
                }
            }
            old is SearchImageListTypeModel.Image && new is SearchImageListTypeModel.Image -> {
                when {
                    old.image.isSelect != new.image.isSelect -> selectPayload
                    else -> super.getChangePayload(oldItemPosition, newItemPosition)
                }
            }
            else -> super.getChangePayload(oldItemPosition, newItemPosition)
        }
    }
}