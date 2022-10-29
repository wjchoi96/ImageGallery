package com.gallery.kakaogallery.presentation.ui.gallery

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.domain.model.ImageListTypeModel
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.presentation.ui.searchimage.GalleryImageItemViewHolder
import com.gallery.kakaogallery.presentation.ui.searchimage.ImageDiffUtilCallback
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class GalleryAdapter(
    private val imageItemSelectListener: (ImageModel, Int) -> Unit
) : RecyclerView.Adapter<GalleryImageItemViewHolder>() {

    enum class Payload {
        Save,
        Select
    }

    private var imageList: List<ImageModel> = emptyList()
    val currentItemSize: Int
        get() = imageList.size

    fun setList(list: List<ImageModel>) {
        imageList = list
    }

    private fun getDiffRes(newList: List<ImageModel>): DiffUtil.DiffResult {
        Timber.d("getDiffRes run at ${Thread.currentThread().name}")
        val diffCallback = ImageDiffUtilCallback(
            this.imageList.map { ImageListTypeModel.Image(it) },
            newList.map { ImageListTypeModel.Image(it) },
            null,
            Payload.Select,
            Payload.Save
        )
        return DiffUtil.calculateDiff(diffCallback)
    }

    private var adapterDisposable: Disposable? = null
    fun updateList(list: List<ImageModel>) {
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
                Timber.d("diff debug updateList post : setList[" + this.currentItemSize + "], newList[" + newList.size + "]")
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
        holder.bind(imageList[position])
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
                Payload.Save -> {
                    Timber.d("payload Save : $position => $position")
                    holder.bindIsSave(imageList[position])
                    holder.bindIsSelect(imageList[position])
                }
                Payload.Select -> {
                    Timber.d("payload Select : $position => $position")
                    holder.bindIsSelect(imageList[position])
                }
            }
        }
    }
}