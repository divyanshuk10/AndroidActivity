package com.example.androidactivity.interfaces

import android.location.Location

/**
 * This interface provides a single callback when the last location is fetched from the GPS provider
 */
interface LastLocationCallbackListener {
    fun onLocationFound(location: Location?)
    fun onFailed()
    fun permissionRequired()
}