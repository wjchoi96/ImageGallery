package com.gallery.kakaogallery.presentation.extension

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast


fun Context.setSoftKeyboardVisible(view: View, visible: Boolean) {
    val imm = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    when (visible) {
        false -> imm.hideSoftInputFromWindow(view.windowToken, 0)
        else -> imm.showSoftInput(view, 0)
    }
}

fun Context.showToast(message: String) {
    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
}