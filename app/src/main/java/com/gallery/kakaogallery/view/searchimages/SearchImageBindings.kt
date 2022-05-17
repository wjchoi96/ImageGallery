package com.gallery.kakaogallery.view.searchimages

import android.graphics.Color
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.model.ImageModel


@BindingAdapter("app:imageTypeSrc")
fun setImageTypeSrc(imageView : ImageView, isImageType : Boolean){
    if(isImageType){
        imageView.setImageResource(R.drawable.ic_video)
    }else{
        imageView.setImageResource(R.drawable.ic_image)
    }
}

@BindingAdapter("app:selectEffect")
fun setSelectEffect(view : View, isSelect : Boolean){
    if(isSelect){
        view.setBackgroundResource(R.drawable.background_select_image)
    }else{
        view.setBackgroundColor(Color.parseColor("#FFFFFF"))
    }
}

@BindingAdapter("app:imageFromUrl")
fun setImageFromUrl(imageView: ImageView, url : String){
    Glide.with(imageView)
        .load(url)
        .error(R.drawable.background_image_error)
        .placeholder(R.drawable.background_image_placeholder)
        .override(imageView.layoutParams.width, imageView.layoutParams.height)
        .into(imageView)
}

@BindingAdapter("app:onEditorActionListener")
fun setOnEditorActionListener(editText : EditText, viewModel : SearchImageViewModel){
    editText.setOnEditorActionListener(viewModel.searchEditorActionListener)
}