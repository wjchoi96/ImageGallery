package com.gallery.kakaogallery.presentation.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gallery.kakaogallery.presentation.ui.LifeCycleLogObserver
import timber.log.Timber

abstract class LifeCycleLoggingActivity: AppCompatActivity() {

    private val logLifecycleObserver = LifeCycleLogObserver(this::class.java.simpleName + " LifecycleLogging")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(logLifecycleObserver)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Timber.tag(logLifecycleObserver.tag).i("onRestoreInstanceState savedInstanceState[$savedInstanceState]")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.tag(logLifecycleObserver.tag).i("onSaveInstanceState")
    }

    override fun onRestart() {
        super.onRestart()
        Timber.tag(logLifecycleObserver.tag).i("onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(logLifecycleObserver)
    }
}