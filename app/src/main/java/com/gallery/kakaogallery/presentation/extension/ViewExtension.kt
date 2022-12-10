package com.gallery.kakaogallery.presentation.extension

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.showSnackBar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun View.showSnackBar(message: String, action: Pair<String, View.OnClickListener>, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).apply {
        setAction(action.first, action.second)
    }.show()
}