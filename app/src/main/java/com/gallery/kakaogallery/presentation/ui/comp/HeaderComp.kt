package com.gallery.kakaogallery.presentation.ui.comp

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.CompHeaderBinding

class HeaderComp constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyle, defStyleRes) {

    constructor(context: Context) : this(context, null, 0, 0)

    private val binding: CompHeaderBinding

    init {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.comp_header, this, true)
        setDefaultVisible()
    }

    private fun setDefaultVisible() {
        binding.btnTvLeft.visibility = View.GONE
        binding.btnTvRight.visibility = View.GONE
        binding.tvTitle.visibility = View.GONE
    }

    fun clearView() {
        binding.btnTvLeft.visibility = View.GONE
        binding.btnTvRight.visibility = View.GONE
        binding.tvTitle.visibility = View.GONE
    }

    fun setBackgroundClickListener(listener: () -> Unit) {
        binding.background.setOnClickListener {
            listener.invoke()
        }
    }

    fun setTitle(title: String?) {
        binding.tvTitle.visibility = View.VISIBLE
        binding.tvTitle.text = title
    }

    fun setRightBtnListener(text: String, listener: () -> Unit) {
        binding.btnTvRight.visibility = View.VISIBLE
        binding.btnTvRight.text = text
        binding.btnTvRight.setOnClickListener {
            listener.invoke()
        }
    }

    fun removeRightBtn() {
        binding.btnTvRight.visibility = View.GONE
    }

    fun setLeftBtnListener(text: String, listener: () -> Unit) {
        binding.btnTvLeft.visibility = View.VISIBLE
        binding.btnTvLeft.text = text
        binding.btnTvLeft.setOnClickListener {
            listener.invoke()
        }
    }

    fun removeLeftBtn() {
        binding.btnTvLeft.visibility = View.GONE
    }
}