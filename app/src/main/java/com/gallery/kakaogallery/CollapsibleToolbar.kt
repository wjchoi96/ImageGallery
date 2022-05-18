package com.gallery.kakaogallery

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.constraintlayout.motion.widget.MotionLayout
import com.google.android.material.appbar.AppBarLayout

/**
 *  모션 레이아웃 학습
 *  https://developer.android.com/training/constraint-layout/motionlayout/examples?hl=ko
 *  https://medium.com/google-developers/introduction-to-motionlayout-part-iii-47cd64d51a5
 */
class CollapsibleToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): MotionLayout(context, attrs, defStyleAttr), AppBarLayout.OnOffsetChangedListener {

    /**
     * 이게 없으면 접히긴하는데, 모션이 안일어난다
     * 저 progress 에 따라서 모션이 이루어지는듯
     */
    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        progress = -verticalOffset / appBarLayout?.totalScrollRange?.toFloat()!!
        Log.d("CollapsibleToolbar", "progress: $progress")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as? AppBarLayout)?.addOnOffsetChangedListener(this)
    }
}