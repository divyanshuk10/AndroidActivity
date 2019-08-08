package com.example.androidactivity.utils

import android.content.Context
import android.location.GpsStatus
import android.location.LocationManager
import com.example.androidactivity.BaseApplication


object GpsChecker {

    fun isGpsEnabled(): Boolean {
        val locationManager: LocationManager =
            BaseApplication.getInstance().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


}