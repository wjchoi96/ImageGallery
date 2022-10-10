package com.gallery.kakaogallery.presentation.ui.gallery

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.ViewImageItemBinding
import com.gallery.kakaogallery.model.ImageModel
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication
import com.gallery.kakaogallery.presentation.ui.base.BaseViewHolder

class GalleryAdapter(
    private val context : Context,
    private val itemSelectListener : (ImageModel, Int) -> (Boolean)
) : RecyclerView.Adapter<GalleryAdapter.GalleryItemViewHolder>() {
    private val TAG = KakaoGalleryApplication.getTag(this::class.java)
    enum class ImagePayload(){
        Save,
        Select
    }

    private var imageList : ArrayList<ImageModel> = ArrayList()
    fun setList(list : ArrayList<ImageModel>){
        imageList.clear()
        imageList.addAll(list)
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
        for(payload in payloads){
            when(payload){
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
        private val vd : ViewImageItemBinding,
        private val context : Context,
        private val itemSelectListener : (ImageModel, Int) -> (Boolean)
    ) : BaseViewHolder(vd){

        fun bind(item : ImageModel){
            loadImage(item.imageThumbUrl)
            vd.tvDateTime.text = item.saveDateTime
            setSaveIcon(item.isSaveImage)
            setSelectEffect(item.isSelect)
            vd.background.setOnClickListener {
                itemSelectListener.invoke(item, adapterPosition)
            }

            if(item.isImageType){
                vd.ivTag.setImageResource(R.drawable.ic_video)
            }else{
                vd.ivTag.setImageResource(R.drawable.ic_image)
            }
        }

        fun setSelectEffect(show : Boolean){
            if(show){
                vd.background.setBackgroundResource(R.drawable.background_select_image)
            }else{
                vd.background.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }
        }

        fun setSaveIcon(isSave : Boolean){
            if(isSave)
                vd.ivStar.visibility = View.VISIBLE
            else
                vd.ivStar.visibility = View.GONE
        }

        private fun loadImage(url : String){
            Glide.with(context)
                .load(url)
                .error(R.drawable.background_image_error)
                .placeholder(R.drawable.background_image_placeholder)
                .override(vd.ivImage.layoutParams.width, vd.ivImage.layoutParams.height)
                .into(vd.ivImage)
        }
    }
}