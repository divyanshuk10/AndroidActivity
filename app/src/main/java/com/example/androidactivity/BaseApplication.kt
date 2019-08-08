package com.example.androidactivity

import android.app.Application
import com.example.androidactivity.utils.InternetConnectivityUtil
import com.example.androidactivity.utils.InternetConnectivityUtil.ConnectivityReceiverListener


class BaseApplication : Application() {

    companion object {
        private lateinit var baseApplication: BaseApplication

        @Synchronized
        fun getInstance(): BaseApplication {
            return baseApplication
        }
    }

    override fun onCreate() {
        super.onCreate()
        baseApplication = this
    }

    fun setConnectivityListener(listener: ConnectivityReceiverListener) {
        InternetConnectivityUtil.connectivityReceiverListener = listener
    }
}
