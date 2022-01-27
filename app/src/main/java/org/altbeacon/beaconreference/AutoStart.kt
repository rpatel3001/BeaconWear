package org.altbeacon.beaconreference

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast


class AutoStart : BroadcastReceiver() {
    override fun onReceive(context: Context, arg1: Intent?) {
        try {
            val intent = Intent(context, BeaconReferenceApplication::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
        }
    }
}