package com.gallery.kakaogallery.presentation.ui.base

import androidx.databinding.ViewDataBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable

abstract class DisposableManageFragment<T : ViewDataBinding> : BaseFragmentUseHandler<T>() {
    protected val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onDetach() {
        super.onDetach()
        if (!compositeDisposable.isDisposed)
            compositeDisposable.dispose()
    }
}