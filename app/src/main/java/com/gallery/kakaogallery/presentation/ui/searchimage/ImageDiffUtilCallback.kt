package com.gallery.kakaogallery.presentation.ui.searchimage

import androidx.recyclerview.widget.DiffUtil
import com.gallery.kakaogallery.domain.model.ImageListTypeModel
import timber.log.Timber

/**
 * https://sohee1702.tistory.com/409
 *
 * 공강 : O(N)
 * 시간 : O(N + D^2)
 * => old/new 두 리스트의 합인 N개 / old가 new로 변환되기 위해 필요한 최소 작업갯수(==edit script) D
 * 최대 사이즈는 2^26(67,108,864)개까지 지원
 */
class ImageDiffUtilCallback(
    private val oldList: List<ImageListTypeModel>,
    private val newList: List<ImageListTypeModel>,
    private val queryPayload: Any?,
    private val selectPayload: Any?,
    private val savePayload: Any?
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
            old is ImageListTypeModel.Query && new is ImageListTypeModel.Query -> {
                if (old.query != new.query) {
                    queryPayload
                } else {
                    super.getChangePayload(oldItemPosition, newItemPosition)
                }
            }
            old is ImageListTypeModel.Image && new is ImageListTypeModel.Image -> {
                when {
                    old.image.isSelect != new.image.isSelect -> selectPayload
                    old.image.isSaveImage != new.image.isSaveImage -> savePayload
                    else -> super.getChangePayload(oldItemPosition, newItemPosition)
                }
            }
            else -> super.getChangePayload(oldItemPosition, newItemPosition)
        }
    }
}