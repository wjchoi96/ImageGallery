package com.gallery.kakaogallery.presentation.ui.gallery

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.ViewImageItemBinding
import com.gallery.kakaogallery.domain.model.ImageModel
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication

class GalleryAdapter(
    private val context: Context,
    private val itemSelectListener: (ImageModel, Int) -> (Boolean)
) : RecyclerView.Adapter<GalleryAdapter.GalleryItemViewHolder>() {
    private val TAG = KakaoGalleryApplication.getTag(this::class.java)

    enum class ImagePayload() {
        Save,
        Select
    }

    private var imageList: List<ImageModel> = emptyList()
    fun setList(list: List<ImageModel>) {
        imageList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryItemViewHolder {
        return GalleryItemViewHolder(
            DataBindingUtil.inflate(
                (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater),
                R.layout.view_image_item,
                parent,
                false
            ), context, itemSelectListener
        )
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: GalleryItemViewHolder, position: Int) {
        holder.bind(imageList[position])
    }

    override fun onBindViewHolder(
        holder: GalleryItemViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        for (payload in payloads) {
            when (payload) {
                ImagePayload.Save -> {
                    Log.d(TAG, "paload Save : $position => $position")
                    holder.setSaveIcon(imageList[position].isSaveImage)
                    holder.setSelectEffect(imageList[position].isSelect)
                }
                ImagePayload.Select -> {
                    Log.d(TAG, "paload Select : $position => $position")
                    holder.setSelectEffect(imageList[position].isSelect)
                }
            }
        }
    }

    class GalleryItemViewHolder(
        private val binding: ViewImageItemBinding,
        private val context: Context,
        private val itemSelectListener: (ImageModel, Int) -> (Boolean)
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ImageModel) {
            loadImage(item.imageThumbUrl)
            binding.tvDateTime.text = item.saveDateTime
            setSaveIcon(item.isSaveImage)
            setSelectEffect(item.isSelect)
            binding.background.setOnClickListener {
                itemSelectListener.invoke(item, adapterPosition)
            }

            if (item.isImageType) {
                binding.ivTag.setImageResource(R.drawable.ic_video)
            } else {
                binding.ivTag.setImageResource(R.drawable.ic_image)
            }
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

        private fun loadImage(url: String) {
            Glide.with(context)
                .load(url)
                .error(R.drawable.bg_image_error)
                .placeholder(R.drawable.bg_image_placeholder)
                .override(binding.ivImage.layoutParams.width, binding.ivImage.layoutParams.height)
                .into(binding.ivImage)
        }
    }
}