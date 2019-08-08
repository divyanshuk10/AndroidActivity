package com.example.androidactivity.interfaces

import android.location.Location

/**
 * This is a helper interface to continuously provide callbacks on location updates
 */
interface LiveLocationCallbackListener {
    fun onLocationUpdate(location: Location?)
    fun permissionRequired()
}