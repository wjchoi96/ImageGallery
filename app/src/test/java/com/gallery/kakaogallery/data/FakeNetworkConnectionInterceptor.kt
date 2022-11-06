package com.gallery.kakaogallery.data

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class FakeNetworkConnectionInterceptor(
    defaultNetworkState: Boolean = true
): Interceptor {
    companion object {
        const val ioExceptionMessage = "network connection fai"
    }
    private var isNetworkAvailable: Boolean = defaultNetworkState
    fun setNetworkState(state: Boolean) {
        isNetworkAvailable = state
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return when (isNetworkAvailable) {
            true -> {
                chain.request().newBuilder().run {
                    chain.proceed(build())
                }
            }
            else -> throw IOException(ioExceptionMessage) // must throw IOException
        }
    }
}