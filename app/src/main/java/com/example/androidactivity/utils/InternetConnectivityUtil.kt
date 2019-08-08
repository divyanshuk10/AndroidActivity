package com.example.androidactivity.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.example.androidactivity.BaseApplication

object InternetConnectivityUtil : BroadcastReceiver() {
    lateinit var connectivityReceiverListener: ConnectivityReceiverListener

    override fun onReceive(context: Context?, intent: Intent?) {
        val conMgr = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = conMgr.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected
        if (connectivityReceiverListener != null)
            InternetConnectivityUtil.connectivityReceiverListener.onNetworkConnectionChanged(isConnected)
    }

    fun isConnectedOrConnecting(): Boolean {
        val conMgr =
            BaseApplication.getInstance().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = conMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

}