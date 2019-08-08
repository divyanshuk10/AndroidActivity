package com.example.androidactivity.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.example.androidactivity.BaseApplication
import com.example.androidactivity.interfaces.LastLocationCallbackListener
import com.example.androidactivity.interfaces.LiveLocationCallbackListener
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

open class GpsLocationUtil {

    companion object {
        var lastKnownLocation: Location? = null
        private const val REQUEST_UPDATE_CHECK_SETTINGS = 300
        private const val REQUEST_CHECK_SETTINGS = 400

    }

    private val mLocationProviderClient: FusedLocationProviderClient
    private val mLocationRequest = LocationRequest()
    private val mLocationCallback: LocationCallback?
    private var mLiveLocationCallbackListener: LiveLocationCallbackListener? = null
    private var mLastLocationCallbackListener: LastLocationCallbackListener? = null
    private val _interval = 2000L
    private val _fastestInterval = 1000L
    private var _activity: Activity? = null
    private val task: Task<LocationSettingsResponse>

    init {
        mLocationRequest.interval = _interval
        mLocationRequest.fastestInterval = _fastestInterval
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationProviderClient = LocationServices.getFusedLocationProviderClient(BaseApplication.getInstance())
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                if (mLiveLocationCallbackListener != null) {
                    mLiveLocationCallbackListener!!.onLocationUpdate(locationResult!!.lastLocation)
                }
            }
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(BaseApplication.getInstance())

        task = client.checkLocationSettings(builder.build())
    }

    /**
     * Use this function to get the last location GPS coordinates
     * And if coordinates are not provided handle it in 'onFailed()' method accordingly
     *
     * @param mCallbackLast
     * @param activity
     * */
    fun getLastLocation(activity: Activity?, mCallbackLast: LastLocationCallbackListener?) {
        mLastLocationCallbackListener = mCallbackLast
        _activity = activity
        if (ActivityCompat.checkSelfPermission(
                BaseApplication.getInstance(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                BaseApplication.getInstance(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            mCallbackLast?.permissionRequired()
            return
        }
        if (_activity != null) {
            /* task.addOnCompleteListener {
                 OnCompleteListener<LocationSettingsResponse> { task ->
                     try {
                         val response = task.getResult(ApiException::class.java)
                         mLocationProviderClient.lastLocation.addOnSuccessListener {
                             OnSuccessListener<Location> { location: Location? ->
                                 if (location != null) {
                                     if (mLastLocationCallbackListener != null) {
                                         mLastLocationCallbackListener!!.onLocationFound(location)
                                     } else {
                                         mLastLocationCallbackListener!!.onFailed()
                                     }
                                 }

                             }
                         }
                     } catch (exception: ApiException) {
                         when (exception.statusCode) {
                             LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                                 // Location settings are not satisfied. But could be fixed by showing the
                                 // user a dialog.
                                 try {
                                     // Cast to a resolvable exception.
                                     val resolvable = exception as ResolvableApiException
                                     // Show the dialog by calling startResolutionForResult(),
                                     // and check the result in onActivityResult().
                                     resolvable.startResolutionForResult(
                                         activity,
                                         REQUEST_CHECK_SETTINGS
                                     )
                                 } catch (e: IntentSender.SendIntentException) {
                                     // Ignore the error.
                                 } catch (e: ClassCastException) {
                                     // Ignore, should be an impossible error.
                                 }

                             LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                             }
                         }// Location settings are not satisfied. However, we have no way to fix the
                         // settings so we won't show the dialog.
                     }
                 }

             }*/
            task.addOnSuccessListener {
                mLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    if (mCallbackLast != null) {
                        if (location != null) {
                            lastKnownLocation = location
                            mCallbackLast.onLocationFound(location)
                        } else
                            mCallbackLast.onFailed()
                    }
                }
            }
            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // try showing a dialog to enable GPS
                    try {
                        // Cast to a resolvable exception.
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        exception.startResolutionForResult(
                            activity,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        //Ignoring error, and disable functionality
                    } catch (e: ClassCastException) {
                        // Ignore, should be an impossible error.
                    }
                }
            }
        }
    }

    /**
     * Use this function to continuously provide location updates after every 2 seconds as mentioned in  '_interval'
     * Always use 'removeUpdateListener()'method to remove location updates listener in 'onPause()' and 'onDestroy()'
     * and reinitialize in 'onResume()'
     *
     * @param mLiveLocationUpdates
     * */
    fun getLiveLocationUpdates(mLiveLocationUpdates: LiveLocationCallbackListener?) {
        if (ActivityCompat.checkSelfPermission(
                BaseApplication.getInstance(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                BaseApplication.getInstance(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            mLiveLocationUpdates?.permissionRequired()
            return
        }
        if (_activity != null) {
            task.addOnSuccessListener {
                mLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
            }
            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // try showing a dialog to enable GPS
                    try {
                        exception.startResolutionForResult(_activity, REQUEST_UPDATE_CHECK_SETTINGS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        //Ignoring error, and disable functionality
                    }
                }
            }
        }
    }

    fun removeUpdateListener() {
        if (mLocationCallback != null)
            mLocationProviderClient.removeLocationUpdates(mLocationCallback)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // val states = LocationSettingsStates.fromIntent(data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> getLastLocation(_activity, mLastLocationCallbackListener)
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(BaseApplication.getInstance(), "GPS is not available", Toast.LENGTH_LONG).show()
                }
                else -> {
                }
            }
            REQUEST_UPDATE_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> getLastLocation(_activity, mLastLocationCallbackListener)
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(BaseApplication.getInstance(), "GPS is not available", Toast.LENGTH_LONG).show()
                }
                else -> {
                }
            }
        }
    }
}