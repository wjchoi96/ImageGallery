package com.gallery.kakaogallery.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.gallery.kakaogallery.presentation.application.KakaoGalleryApplication
import timber.log.Timber

class NetworkUtil private constructor() : ConnectivityManager.NetworkCallback() {
    companion object {
        val instance = NetworkUtil()
    }

    private val connManager: ConnectivityManager =
        (KakaoGalleryApplication.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
    private val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR) // TRANSPORT_CELLULAR : 이 네트워크가 셀룰러 전송을 사용함을 나타냅니다.
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)       // TRANSPORT_WIFI : 이 네트워크가 Wi-Fi 전송을 사용함을 나타냅니다.
        .build()

    fun register() {
        connManager.registerNetworkCallback(networkRequest, this)
        Timber.d("register network util")
    }

    fun unregister() {
        connManager.unregisterNetworkCallback(this)
        Timber.d("unregister network util")
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        Timber.d("############## network on ######################")
        KakaoGalleryApplication.isOnline = true
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        Timber.d("############## network off ######################")
        KakaoGalleryApplication.isOnline = false
    }
}