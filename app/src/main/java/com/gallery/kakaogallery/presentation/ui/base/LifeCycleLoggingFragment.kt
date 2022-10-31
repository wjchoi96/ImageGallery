package com.gallery.kakaogallery.presentation.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gallery.kakaogallery.presentation.ui.LifeCycleLogObserver
import timber.log.Timber

abstract class LifeCycleLoggingFragment: Fragment() {

    private val logLifecycleObserver = LifeCycleLogObserver(this::class.java.simpleName)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.tag(logLifecycleObserver.tag).i("onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(logLifecycleObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.tag(logLifecycleObserver.tag).i("onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.tag(logLifecycleObserver.tag).i("onViewCreated")
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Timber.tag(logLifecycleObserver.tag).i("onViewStateRestored savedInstanceState[$savedInstanceState]")
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Timber.tag(logLifecycleObserver.tag).i("onHiddenChanged to [$hidden]")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.tag(logLifecycleObserver.tag).i("onSaveInstanceState")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.tag(logLifecycleObserver.tag).i("onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(logLifecycleObserver)
    }

    override fun onDetach() {
        super.onDetach()
        Timber.tag(logLifecycleObserver.tag).i("onDetach")
    }

}