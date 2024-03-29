package com.gallery.kakaogallery.presentation.util

import android.app.AlertDialog
import android.content.Context

/*
    bottom sheet dialog
    https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetDialog
    https://velog.io/@eoqkrskfk94/Bottom-Sheet-Dialog-%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0

    set round, margin
    https://stackoverflow.com/questions/55218663/customization-bottom-sheet-dialogs-view

    bottom sheet dialog fragment
    https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetDialogFragment
    https://stackoverflow.com/questions/55218663/customization-bottom-sheet-dialogs-view
 */
object DialogUtil {
    fun show(context: Context, content: String, positiveBtn: String, positiveListener: () -> Unit) {
        AlertDialog.Builder(context).apply {
            setCancelable(false)
            setMessage(content)
            setPositiveButton(positiveBtn) { _, _ ->
                positiveListener.invoke()
            }
        }.create().apply {
            show()
        }
    }

    fun show(
        context: Context,
        content: String,
        positiveBtn: String,
        negativeBtn: String,
        positiveListener: () -> Unit,
        negativeListener: () -> Unit
    ) {
        AlertDialog.Builder(context).apply {
            setCancelable(false)
            setMessage(content)
            setPositiveButton(positiveBtn) { _, _ ->
                positiveListener.invoke()
            }
            setNegativeButton(negativeBtn) { _, _ ->
                negativeListener.invoke()
            }
        }.create().apply {
            show()
        }
    }
}