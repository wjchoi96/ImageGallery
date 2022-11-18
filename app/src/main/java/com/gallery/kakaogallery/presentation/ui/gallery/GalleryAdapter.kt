package com.gallery.kakaogallery.presentation.ui.gallery

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.domain.model.GalleryImageModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.presentation.ui.searchimage.GalleryImageItemViewHolder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class GalleryAdapter(
    private val imageItemSelectListener: (ImageModel, Int) -> Unit
) : RecyclerView.Adapter<GalleryImageItemViewHolder>() {

    enum class Payload {
        Select
    }

    private var imageList: List<GalleryImageModel> = emptyList()

    fun setList(list: List<GalleryImageModel>) {
        imageList = list
    }

    private fun getDiffRes(newList: List<GalleryImageModel>): DiffUtil.DiffResult {
        Timber.d("getDiffRes run at ${Thread.currentThread().name}")
        val diffCallback = GalleryImageDiffUtilCallback(
            this.imageList,
            newList,
            Payload.Select
        )
        return DiffUtil.calculateDiff(diffCallback)
    }

    private var adapterDisposable: Disposable? = null
    fun updateList(list: List<GalleryImageModel>) {
        adapterDisposable?.dispose()
        adapterDisposable = null

        val newList = list.toList()
        Timber.d("diff debug updateList called oldList[${imageList.size}], newList[${newList.size}]")
        adapterDisposable = Observable.fromCallable{
            getDiffRes(newList)
        }.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("getDiffRes subscribe run at ${Thread.currentThread().name}")
                this.setList(newList) // must call main thread
                it.dispatchUpdatesTo(this)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryImageItemViewHolder {
        return GalleryImageItemViewHolder.from(
            parent,
            imageItemSelectListener
        )
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: GalleryImageItemViewHolder, position: Int) {
        holder.bind(imageList[position], true)
    }

    override fun onBindViewHolder(
        holder: GalleryImageItemViewHolder,
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
                    Timber.d("payload Select : $position => $position")
                    holder.bindIsSelect(imageList[position])
                }
            }
        }
    }
}