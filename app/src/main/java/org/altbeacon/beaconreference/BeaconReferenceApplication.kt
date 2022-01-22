package org.altbeacon.beaconreference

import android.app.*
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import org.altbeacon.beacon.*
import org.altbeacon.bluetooth.BluetoothMedic
import android.bluetooth.le.AdvertiseSettings

import android.bluetooth.le.AdvertiseCallback

import org.altbeacon.beacon.BeaconTransmitter

import org.altbeacon.beacon.BeaconParser

import org.altbeacon.beacon.Beacon


class BeaconReferenceApplication: Application(), MonitorNotifier {
    lateinit var region: Region

    override fun onCreate() {
        super.onCreate()

        val beaconManager = BeaconManager.getInstanceForApplication(this)

        // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
        // find a different type of beacon, you must specify the byte layout for that beacon's
        // advertisement with a line like below.  The example shows how to find a beacon with the
        // same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb.  To find the proper
        // layout expression for other beacon types, do a web search for "setBeaconLayout"
        // including the quotes.
        //
        //beaconManager.getBeaconParsers().clear();
        //beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:0-1=4c00,i:2-24v,p:24-24"));

        // The example shows how to find iBeacon.
        beaconManager.beaconParsers.add(
            BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))

        // enabling debugging will send lots of verbose debug information from the library to Logcat
        // this is useful for troubleshooting problems
        // BeaconManager.setDebug(true)


        // The BluetoothMedic code here, if included, will watch for problems with the bluetooth
        // stack and optionally:
        // - power cycle bluetooth to recover on bluetooth problems
        // - periodically do a proactive scan or transmission to verify the bluetooth stack is OK
        BluetoothMedic.getInstance().enablePowerCycleOnFailures(this)
        // BluetoothMedic.getInstance().enablePeriodicTests(this, BluetoothMedic.SCAN_TEST + BluetoothMedic.TRANSMIT_TEST)

        // By default, the library will scan in the background every 5 minutes on Android 4-7,
        // which will be limited to scan jobs scheduled every ~15 minutes on Android 8+
        // If you want more frequent scanning (requires a foreground service on Android 8+),
        // configure that here.
        // If you want to continuously range beacons in the background more often than every 15 minutes,
        // you can use the library's built-in foreground service to unlock this behavior on Android
        // 8+.   the method below shows how you set that up.
        setupForegroundService()

        beaconManager.setEnableScheduledScanJobs(false)

        // The following code block effectively disables beacon scanning in the foreground service
        // to save battery.  Do not include this code block if you want to detect beacons
        beaconManager.beaconParsers.clear() // clearing all beacon parsers ensures nothing matches
        beaconManager.backgroundBetweenScanPeriod = Long.MAX_VALUE
        beaconManager.backgroundScanPeriod = 0
        beaconManager.foregroundBetweenScanPeriod = Long.MAX_VALUE
        beaconManager.foregroundScanPeriod = 0

        // The following code block activates the foreground service by starting background scanning
        region = Region("dummy-region", Identifier.parse("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"), null, null)
        beaconManager.startMonitoring(region)
        beaconManager.addMonitorNotifier(this)

        // This code block starts beacon transmission
        val beacon = Beacon.Builder()
            .setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6")
            .setId2("1")
            .setId3("2")
            .setManufacturer(0x0118) // Radius Networks.  Change this for other beacon layouts
            .setTxPower(-59)
            .setDataFields(listOf(0)) // Remove this for beacon layouts without d: fields
            .build()

        // Change the layout below for other beacon types
        val beaconParser = BeaconParser()
            .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        val beaconTransmitter = BeaconTransmitter(applicationContext, beaconParser)
        beaconTransmitter.startAdvertising(beacon, object : AdvertiseCallback() {
            override fun onStartFailure(errorCode: Int) {
                Log.e(TAG, "Advertisement start failed with code: $errorCode")
            }

            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.i(TAG, "Advertisement start succeeded.")
            }
        })
    }

    private fun setupForegroundService() {
        val builder = Notification.Builder(this, "BeaconReferenceApp")
        builder.setSmallIcon(R.drawable.ic_launcher_background)
        builder.setContentTitle("Scanning for Beacons")
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)
        val channel =  NotificationChannel("beacon-ref-notification-id",
            "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "My Notification Channel Description"
        val notificationManager =  getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        builder.setChannelId(channel.id)
        BeaconManager.getInstanceForApplication(this).enableForegroundServiceScanning(builder.build(), 456)
    }

    private val centralMonitoringObserver = Observer<Int> { state ->
        if (state == MonitorNotifier.OUTSIDE) {
            Log.d(TAG, "outside beacon region: $region")
        }
        else {
            Log.d(TAG, "inside beacon region: $region")
            sendNotification()
        }
    }

    private val centralRangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d(MainActivity.TAG, "Ranged: ${beacons.count()} beacons")
        for (beacon: Beacon in beacons) {
            Log.d(TAG, "$beacon about ${beacon.distance} meters away")
        }
    }

    private fun sendNotification() {
        val builder = NotificationCompat.Builder(this, "beacon-ref-notification-id")
            .setContentTitle("Beacon Reference Application")
            .setContentText("A beacon is nearby.")
            .setSmallIcon(R.drawable.ic_launcher_background)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(Intent(this, MainActivity::class.java))
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(resultPendingIntent)
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }

    companion object {
        const val TAG = "BeaconReference"
    }

    override fun didEnterRegion(region: Region?) {}
    override fun didExitRegion(region: Region?) {}
    override fun didDetermineStateForRegion(state: Int, region: Region?) {}

}