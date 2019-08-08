package com.example.androidactivity.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import com.example.androidactivity.BaseApplication;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationUtil {
    private static final long INTERVAL = 2000L;
    private static final long FASTEST_INTERVAL = 1000L;
    private static final int REQUEST_CHECK_SETTINGS = 300;
    private FusedLocationProviderClient mLocationProviderClient;
    private static Location lastKnownLocation;
    private LocationRequest locationRequest = new LocationRequest();
    private LocationCallback locationCallback;
    private LocationUpdateListner mLocationUpdateListener;
    private Activity activity;
    private MyLocationCallback myLocationCallback;


    public interface MyLocationCallback {
        void onLocationFound(Location location);

        void onFailed();

        void permissionRequired();
    }

    public interface LocationUpdateListner {
        void onLocationUpdate(Location location);

        void permissionRequired();
    }

    public LocationUtil() {
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (mLocationUpdateListener != null) {
                    mLocationUpdateListener.onLocationUpdate(locationResult.getLastLocation());
                }

            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        };
        mLocationProviderClient = LocationServices.getFusedLocationProviderClient(BaseApplication.Companion.getInstance());
    }


    public void getLocation(@Nullable final Activity activity, final MyLocationCallback mCallback) {
        this.activity = activity;
        this.myLocationCallback = mCallback;

        if (ActivityCompat.checkSelfPermission(BaseApplication.Companion.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(BaseApplication.Companion.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (mCallback != null) {
                mCallback.permissionRequired();
            }

            return;


        }

        if (activity != null) {

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            Task<LocationSettingsResponse> result =
                    LocationServices.getSettingsClient(BaseApplication.Companion.getInstance()).checkLocationSettings(builder.build());

            result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @SuppressLint("MissingPermission")
                @Override
                public void onComplete(Task<LocationSettingsResponse> task) {
                    try {
                        LocationSettingsResponse response = task.getResult(ApiException.class);
                        mLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {

                                if (mCallback != null) {
                                    if (location != null) {
                                        lastKnownLocation = location;
                                        mCallback.onLocationFound(location);
                                    } else mCallback.onFailed();
                                }

                            }
                        });


                    } catch (ApiException exception) {
                        switch (exception.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied. But could be fixed by showing the
                                // user a dialog.
                                try {
                                    // Cast to a resolvable exception.
                                    ResolvableApiException resolvable = (ResolvableApiException) exception;
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    resolvable.startResolutionForResult(
                                            activity,
                                            REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                } catch (ClassCastException e) {
                                    // Ignore, should be an impossible error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.

                                break;
                        }
                    }
                }
            });
        }


    }

    @Nullable
    public static Location getLastKnowLocation() {
        return lastKnownLocation;
    }

    /**
     * Get live location update.  Please remove `removeUpdateListener` onPouse or OnStop
     *
     * @param mLocationUpdateListener
     */
    public void getLocatinUpdate(LocationUpdateListner mLocationUpdateListener) {
        this.mLocationUpdateListener = mLocationUpdateListener;
        if (ActivityCompat.checkSelfPermission(BaseApplication.Companion.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(BaseApplication.Companion.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (mLocationUpdateListener != null)
                mLocationUpdateListener.permissionRequired();
            return;
        }
        mLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    /**
     *
     */

    public void removeUpdateListener() {
        if (locationCallback != null) {
            mLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getLocation(activity, myLocationCallback);

                        break;
                    case Activity.RESULT_CANCELED:
                        ///

                        break;
                    default:
                        break;
                }
                break;
        }
    }


}
