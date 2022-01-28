package org.altbeacon.beaconreference

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val freq: Spinner = findViewById(R.id.freq)
        val freqAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                arrayOf("1 Hz", "3 Hz", "10 Hz"))
        freq.adapter = freqAdapter

        val mode: Spinner = findViewById(R.id.mode)
        val modeAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                arrayOf("Ultra Low", "Low", "Medium", "High"))
        mode.adapter = modeAdapter
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }
    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        checkPermissions()
    }

    fun broadcastButtonTapped(view: View) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (i in 1 until permissions.size) {
            Log.d(TAG, "onRequestPermissionResult for "+permissions[i]+":" +grantResults[i])
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            val permissionRationale ="This app needs permission to broadcast. "

            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_ADVERTISE)) {
                    val builder =
                        AlertDialog.Builder(this)
                    builder.setTitle("Permission Required")
                    builder.setMessage(permissionRationale)
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener {
                        requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE), 1)
                    }
                    builder.show()
                }
                else {
                    val builder =
                        AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since BLE Advertise permission have not been granted, this app will not be able to broadcast.  Please go to Settings -> Applications -> Permissions and grant Bluetooth permissions to this app.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }

}