package com.gallery.kakaogallery.presentation.extension

import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.safeScrollToTop(smoothScroll: Boolean){
    if (adapter?.itemCount != null && adapter?.itemCount != 0) {
        when (smoothScroll) {
            true -> smoothScrollToPosition(0)
            else -> scrollToPosition(0)
        }

    }
}