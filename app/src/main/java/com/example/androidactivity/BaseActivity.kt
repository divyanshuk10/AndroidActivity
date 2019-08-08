package com.example.androidactivity

import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.example.androidactivity.utils.InternetConnectivityUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest


abstract class BaseActivity : AppCompatActivity(), InternetConnectivityUtil.ConnectivityReceiverListener {

    private lateinit var activityContainer: FrameLayout
    private lateinit var _tvActivityTitle: TextView
    private lateinit var _ivLogo: ImageView
    private lateinit var _toolbar: android.support.v7.widget.Toolbar
    abstract fun getLayoutResId(): Int
    private lateinit var snackbar: Snackbar
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest


    private val appPermission = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val PREMISSION_REQUEST_CODE = 1240

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showSnack(isConnected)
    }

    fun checkAndRequestPermission(): Boolean {
        val listPermissionsRequired = ArrayList<String>()
        for (item: String in appPermission) {
            if (ContextCompat.checkSelfPermission(this, item) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsRequired.add(item)
            }
        }
        if (listPermissionsRequired.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsRequired.toArray(arrayOfNulls<String>(listPermissionsRequired.size)),
                PREMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        _toolbar = findViewById(R.id.toolbar)
        _ivLogo = findViewById(R.id.iv_logo)
        _tvActivityTitle = findViewById(R.id.tv_title)
        activityContainer = findViewById(R.id.layout_container)
        layoutInflater.inflate(getLayoutResId(), activityContainer, true)
        setSupportActionBar(_toolbar)
        setToolbarLogo(R.drawable.ncs_round_corner)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_action_arrow_back)

        fusedLocationProviderClient = FusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.fastestInterval = 2000
        locationRequest.interval = 4000

    }

    override fun onResume() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)

        val connectivityReceiver = InternetConnectivityUtil
        this.registerReceiver(connectivityReceiver, intentFilter)
        BaseApplication.getInstance().setConnectivityListener(this)
        super.onResume()
    }

    fun setScreenTitle(title: String) {
        _tvActivityTitle.text = title
    }

    fun setToolbarLogo(resId: Int) {
        _ivLogo.setImageResource(resId)
    }

    fun setToolbarVisibility(visibility: Int) {
        _toolbar.visibility = visibility
    }

    // Showing the status in Snack bar
    fun showSnack(isConnected: Boolean) {
        val message: String
        val color: Int
        if (isConnected) {
            message = "Good! Connected to Internet"
            color = Color.WHITE
        } else {
            message = "Sorry! Not connected to internet"
            color = Color.RED
        }

        snackbar = Snackbar.make(findViewById(R.id.layout_container), message, Snackbar.LENGTH_LONG)

        val sbView = snackbar.view
        val textView = sbView.findViewById(android.support.design.R.id.snackbar_text) as TextView
        textView.setTextColor(color)
        snackbar.show()
    }

    // Request permissions and asks for them, if not granted or denied
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PREMISSION_REQUEST_CODE) {
            val deniedPermission = HashMap<String, Int>()
            var deniedCount = 0

            // Gather permission grant result
            for (i in grantResults.indices) {
                // Add only permission which are denied
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    deniedPermission[permissions[i]] = grantResults[i]
                    deniedCount++
                }
            }
            // Check if all permission are granted
            if (deniedCount == 0) {
                initApp()
            }// Either all or one permission is denied
            else {
                for (item in deniedPermission) {
                    val permName = item.key

                    // Permission denied ("never ask again is not checked")
                    // So, Asking again
                    // shouldShowRequestPermissionRationale will return true
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {
                        // Show Dialog
                        this.showAlertDialog(
                            "",
                            " This app needs Location and Storage permissions to work without problem ",
                            "Yes, Grant Permissions",
                            DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                                dialog.dismiss()
                                checkAndRequestPermission()
                            },
                            "No, Exit App",
                            DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                                dialog.dismiss()
                                finish()
                            }, false
                        )
                    }
                    // Permission is denied (and never ask is checked)
                    // shouldShowRequestPermissionRationale will return false
                    else {
                        // Show Dialog
                        showAlertDialog(
                            "",
                            " You have denied some permissions. Allow all permissions at [Setting] > [Permissions] ",
                            "Go to Settings",
                            DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                                dialog.dismiss()

                                val settingsIntent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", packageName, null)
                                )
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(settingsIntent)
                                finish()

                            },
                            "No, Exit App",
                            DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                                dialog.dismiss()
                                finish()
                            }, false
                        )
                        break
                    }
                }
            }
        }
    }

    // Show Alert dialog box
    fun showAlertDialog(
        title: String, message: String, positiveLabel: String, positiveClick: DialogInterface.OnClickListener,
        negativeLabel: String, negativeClick: DialogInterface.OnClickListener, isCancelable: Boolean
    ): AlertDialog {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setCancelable(isCancelable)
        builder.setMessage(message)
        builder.setNegativeButton(negativeLabel, negativeClick)
        builder.setPositiveButton(positiveLabel, positiveClick)
        val alert = builder.create()
        alert.show()
        return alert
    }

    private fun initApp() {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}