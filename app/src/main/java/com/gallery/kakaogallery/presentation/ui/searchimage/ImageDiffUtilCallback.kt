package com.gallery.kakaogallery.presentation.ui.searchimage

import androidx.recyclerview.widget.DiffUtil
import com.gallery.kakaogallery.model.ImageModel
import com.gallery.kakaogallery.presentation.ui.searchimage.SearchImagesAdapter

/**
 * https://sohee1702.tistory.com/409
 *
 * 공강 : O(N)
 * 시간 : O(N + D^2)
 * => old/new 두 리스트의 합인 N개 / old가 new로 변환되기 위해 필요한 최소 작업갯수(==edit script) D
 * 최대 사이즈는 2^26(67,108,864)개까지 지원
 */
class ImageDiffUtilCallback(
    private val oldList: List<ImageModel>,
    private val newList: List<ImageModel>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].imageUrl == newList[newItemPosition].imageUrl
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    //https://blog.yatopark.net/2017/05/02/diffutil%EC%9D%84-%ED%86%B5%ED%95%B4-recyclerview-%EA%B0%B1%EC%8B%A0%EC%9D%84-%ED%9A%A8%EC%9C%A8%EC%A0%81%EC%9C%BC%EB%A1%9C-%EC%B2%98%EB%A6%AC%ED%95%98%EA%B8%B0/
    //areItemsTheSame() && !areContentsTheSame()인 경우 호출
    //default 는 null 리턴중
    //여기서 payload 를 리턴해줘서 적용이되나본데?
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return when {
            old.isSelect != new.isSelect -> SearchImagesAdapter.ImagePayload.Select
            old.isSaveImage != new.isSaveImage -> SearchImagesAdapter.ImagePayload.Select
            else -> super.getChangePayload(oldItemPosition, newItemPosition)
        }
    }
}