package com.example.androidactivity

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.androidactivity.interfaces.LastLocationCallbackListener
import com.example.androidactivity.interfaces.LiveLocationCallbackListener
import com.example.androidactivity.utils.GpsLocationUtil
import com.example.androidactivity.utils.InternetConnectivityUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private lateinit var btnCheckInternet: Button
    private lateinit var latitude: String
    private lateinit var longitude: String
    private lateinit var locationUtil: GpsLocationUtil
    private lateinit var liveLocationCallbackListener: LiveLocationCallbackListener
    private lateinit var lastlocationCallbackListenerLast: LastLocationCallbackListener


    override fun getLayoutResId(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenTitle("Main Activity")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        setToolbarVisibility(View.VISIBLE)
        setToolbarLogo(R.drawable.ncs_round_corner)
        locationUtil = GpsLocationUtil()
        initLocationFeeds()
        val isPermissionsGranted = checkAndRequestPermission()
        if (isPermissionsGranted) {
            Toast.makeText(this, "All Permissions are granted", Toast.LENGTH_LONG).show()

        } else {
            checkAndRequestPermission()
        }

        /*var i: Int = 0
        liveLocationCallbackListener = object : LiveLocationCallbackListener {
            override fun onLocationUpdate(location: Location?) {
                i += 1
                textView.text = "Location Update - $i  - " + location?.latitude.toString()
            }

            override fun permissionRequired() {

            }
        }



        if (isPermissionsGranted) {
            locationUtil.getLastLocation(this, object : LastLocationCallbackListener {
                override fun onLocationFound(location: Location?) {
                    Log.i("LOCATION LAT:", location?.latitude.toString())
                    textView.text = "Location Found -" + location?.latitude.toString()
                }

                override fun onFailed() {

                }

                override fun permissionRequired() {

                }

            })
            //locationUtil.getLocatinUpdate(liveLocationCallbackListner)
        }
*/
        btnCheckInternet = findViewById(R.id.btn_checkInternet)
        btnCheckInternet.setOnClickListener {
            val isConnected = InternetConnectivityUtil.isConnectedOrConnecting()
            locationUtil.getLastLocation(this, lastlocationCallbackListenerLast)
            // locationUtil.getLiveLocationUpdates(liveLocationCallbackListener)
            showSnack(isConnected)
        }

    }

    @SuppressLint("SetTextI18n")
    fun initLocationFeeds() {
        var i: Int = 0
        lastlocationCallbackListenerLast = object : LastLocationCallbackListener {
            override fun onLocationFound(location: Location?) {
                i++
                textView.text = "$i : ${location!!.latitude} - ${location.longitude}"
            }

            override fun onFailed() {
                Toast.makeText(BaseApplication.getInstance(), "Location GPS Disabled", Toast.LENGTH_LONG).show()
            }

            override fun permissionRequired() {
            }
        }
/*
        liveLocationCallbackListener = object : LiveLocationCallbackListener {
            override fun onLocationUpdate(location: Location?) {
                i++
                textView.text = "$i : ${location!!.latitude} - ${location.longitude}"
            }

            override fun permissionRequired() {
            }
        }
*/

    }

    override fun onResume() {
        super.onResume()
        //    locationUtil.getLiveLocationUpdates(liveLocationCallbackListener)
    }

    override fun onPause() {
        super.onPause()
        locationUtil.removeUpdateListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        locationUtil.onActivityResult(requestCode, resultCode, data)
    }


}
