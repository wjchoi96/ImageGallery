package com.gallery.kakaogallery.presentation.ui.dialog

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.gallery.kakaogallery.R
import com.gallery.kakaogallery.databinding.DialogImageManageBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


// https://stackoverflow.com/questions/55218663/customization-bottom-sheet-dialogs-view
// margin style => https://stackoverflow.com/questions/37640031/android-bottom-sheet-layout-margin
// background null set => https://stackoverflow.com/questions/39670847/how-to-set-left-and-right-margin-in-buttomsheetdialogfragment-android
class ImageManageBottomSheetDialog : BottomSheetDialog {
    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, themeResId: Int) : super(context, themeResId) {
        initView()
    }

    lateinit var binding: DialogImageManageBinding

    private fun initView() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_image_manage,
            null,
            false
        )
        setContentView(binding.root)
        (binding.root.parent as View).setBackgroundColor(Color.parseColor("#00000000"))
        setDefaultListener()

        // 가로모드에서 접혀서 보이는 상태로 처리되는 문제 때문에 강제로 state 설정
        behavior.state = BottomSheetBehavior.STATE_EXPANDED // 완전히 펼쳐진 상태
    }

    private fun setDefaultListener() {
        binding.btnPositive.setOnClickListener { this.dismiss() }
        binding.btnNegative.setOnClickListener { this.dismiss() }
    }

    fun setPositiveBtn(btnText: String? = null, positiveListener: ((View) -> (Unit))? = null) {
        if (!btnText.isNullOrBlank())
            binding.btnPositive.text = btnText
        binding.btnPositive.setOnClickListener {
            positiveListener?.invoke(it)
            this.dismiss()
        }
    }

    fun setNegativeBtn(btnText: String? = null, negativeListener: ((View) -> (Unit))? = null) {
        if (!btnText.isNullOrBlank())
            binding.btnNegative.text = btnText
        binding.btnNegative.setOnClickListener {
            negativeListener?.invoke(it)
            this.dismiss()
        }
    }

    fun setContent(content: String?) {
        binding.tvContent.text = content
    }
}