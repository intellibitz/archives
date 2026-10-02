/**
 *
 */
package com.uc.dca.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo.DetailedState
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Binder
import android.os.IBinder
import android.telephony.CellLocation
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import android.telephony.gsm.GsmCellLocation
import android.util.Log
import com.uc.dca.content.IncidentReport
import com.uc.dca.util.HttpHandler
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * @author muthu
 *
 */
class IRService : Service() {

    private val localBinder: IBinder = LocalBinder()
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null
    private var locationManager: LocationManager? = null
    private var wifiManager: WifiManager? = null

    private var batteryInfo: String? = null

    private val batteryChangedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // TODO Auto-generated method stub
            val level = intent.getIntExtra("level", 0)
            batteryInfo = "Battery Level = " + level.toString() + "%"
            Log.i("BatteryStatus: ", batteryInfo)
            val health = intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN)
            when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD ->
                    batteryInfo = batteryInfo!!.concat(" Health = Good")
                BatteryManager.BATTERY_HEALTH_DEAD ->
                    batteryInfo = batteryInfo!!.concat(" Health = Dead")
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE ->
                    batteryInfo = batteryInfo!!.concat(" Health = OverVoltage")
                BatteryManager.BATTERY_HEALTH_OVERHEAT ->
                    batteryInfo = batteryInfo!!.concat(" Health = Overheat")
                BatteryManager.BATTERY_HEALTH_UNKNOWN ->
                    batteryInfo = batteryInfo!!.concat(" Health = Unknown")
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE ->
                    batteryInfo = batteryInfo!!.concat(" Health = Unspecified Failure")
            }
        }
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : object : Binder() {
        fun getService(): IRService {
            return this@IRService
        }
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
    override fun onBind(intent: Intent): IBinder {
        // TODO Auto-generated method stub
        return localBinder
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
    override fun onStart(intent: Intent, startId: Int) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId)
        startIncidentReports(startId)
    }

    /* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
    override fun onCreate() {
        // TODO Auto-generated method stub
        super.onCreate()
        this.registerReceiver(
            batteryChangedReceiver, IntentFilter(
                Intent.ACTION_BATTERY_CHANGED
            )
        )
    }

    /* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
    override fun onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy()
        this.unregisterReceiver(batteryChangedReceiver)
    }

    /**
     *
     */
    private fun initManagers() {
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        wifiManager = getSystemService(WIFI_SERVICE) as WifiManager
    }

    /**
     * @param startId
     */
    private fun startIncidentReports(startId: Int) {
        Log.i(TAG, "Service started - $startId")
        if (null == phoneStateListener) {
            initManagers()
            registerPhoneStateListener()
        }
    }

    private fun registerPhoneStateListener() {
        Log.i(TAG, "PhoneStateListener registered with TelephonyManager ")

        phoneStateListener = object : PhoneStateListener() {

            /*
			 * (non-Javadoc)
			 *
			 * @see android.telephony.PhoneStateListener#onCallStateChanged(int,
			 * java.lang.String)
			 */
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                // TODO Auto-generated method stub
                super.onCallStateChanged(state, incomingNumber)
                Log.i(
                    "onCallStateChanged: ",
                    " ==> =================================================================== <=="
                )
                when (state) {
                    TelephonyManager.CALL_STATE_IDLE -> {
                        Log.i("onCallStateChanged: ", "==> No Call Activity")
                        storeCallStateChange("IDLE - No Call activity.")
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        Log.i("onCallStateChanged: ", "==> Call in Progress")
                        storeCallStateChange("OFF-HOOK - At least one call exists that is dialing, active, or on hold, and no calls are ringing or waiting.")
                    }
                    TelephonyManager.CALL_STATE_RINGING -> {
                        Log.i(
                            "onCallStateChanged: ",
                            "==> Incoming Call - Ringing"
                        )
                        storeCallStateChange("RINGING - A new call arrived and is ringing or waiting. In the latter case, another call is already active.")
                    }
                }
                Log.i(
                    "onCallStateChanged: ",
                    " ==> =================================================================== <=="
                )
            }

            /*
			 * (non-Javadoc)
			 *
			 * @see android.telephony.PhoneStateListener#onCellLocationChanged
			 * (android.telephony.CellLocation)
			 */
            override fun onCellLocationChanged(location: CellLocation) {
                // TODO Auto-generated method stub
                super.onCellLocationChanged(location)
                val gsmCellLocation = location as GsmCellLocation
                Log.i(
                    "onCellLocationChanged: ", gsmCellLocation.cid
                            .toString() + " <==> " + gsmCellLocation.lac
                )
            }

            /*
			 * (non-Javadoc)
			 *
			 * @see
			 * android.telephony.PhoneStateListener#onDataConnectionStateChanged
			 * (int)
			 */
            override fun onDataConnectionStateChanged(state: Int) {
                // TODO Auto-generated method stub
                super.onDataConnectionStateChanged(state)
                Log.i(
                    "onDataConnectionStateChanged: ",
                    " ==> =================================================================== <=="
                )
                when (state) {
                    TelephonyManager.DATA_DISCONNECTED -> {
                        Log.i(
                            "onDataConnectionStateChanged: ",
                            "==> Data is DISCONNECTED"
                        )
                        storeDataConnectionStateChange("DISCONNECTED")
                    }
                    TelephonyManager.DATA_CONNECTED -> {
                        Log.i(
                            "onDataConnectionStateChanged: ",
                            "==> Data is CONNECTED"
                        )
                        storeDataConnectionStateChange("CONNECTED")
                    }
                    TelephonyManager.DATA_CONNECTING -> {
                        Log.i(
                            "onDataConnectionStateChanged: ",
                            "==> Data is CONNECTING"
                        )
                        storeDataConnectionStateChange("CONNECTING")
                    }
                    TelephonyManager.DATA_SUSPENDED -> {
                        Log.i(
                            "onDataConnectionStateChanged: ",
                            "==> Data is SUSPENDED"
                        )
                        storeDataConnectionStateChange("SUSPENDED")
                    }
                }
//				collectTelephonyManagerStats();
                Log.i(
                    "onDataConnectionStateChanged: ",
                    " ==> =================================================================== <=="
                )
            }

            /*
			 * (non-Javadoc)
			 *
			 * @see android.telephony.PhoneStateListener#onServiceStateChanged
			 * (android.telephony.ServiceState)
			 */
            override fun onServiceStateChanged(serviceState: ServiceState) {
                // TODO Auto-generated method stub
                super.onServiceStateChanged(serviceState)
                Log.i(
                    "onServiceStateChanged: ",
                    " ==> =================================================================== <=="
                )
                storeServiceStateChange(serviceState)
                Log.i(
                    "onServiceStateChanged: ",
                    " ==> =================================================================== <=="
                )
            }

            /*
			 * (non-Javadoc)
			 *
			 * @see android.telephony.PhoneStateListener#onSignalStrengthChanged
			 * (int)
			 */
            override fun onSignalStrengthChanged(asu: Int) {
                // TODO Auto-generated method stub
                super.onSignalStrengthChanged(asu)
                storeSignalStrengthChange(asu)
            }

            /*
			 * (non-Javadoc)
			 *
			 * @seeandroid.telephony.PhoneStateListener#
			 * onCallForwardingIndicatorChanged(boolean)
			 */
            override fun onCallForwardingIndicatorChanged(cfi: Boolean) {
                // TODO Auto-generated method stub
                super.onCallForwardingIndicatorChanged(cfi)
            }

            /*
			 * (non-Javadoc)
			 *
			 * @see android.telephony.PhoneStateListener#onDataActivity(int)
			 */
            override fun onDataActivity(direction: Int) {
                // TODO Auto-generated method stub
                super.onDataActivity(direction)
            }

            /*
			 * (non-Javadoc)
			 *
			 * @seeandroid.telephony.PhoneStateListener#
			 * onMessageWaitingIndicatorChanged(boolean)
			 */
            override fun onMessageWaitingIndicatorChanged(mwi: Boolean) {
                // TODO Auto-generated method stub
                super.onMessageWaitingIndicatorChanged(mwi)
            }
        }
        telephonyManager!!.listen(
            phoneStateListener,
            PhoneStateListener.LISTEN_CALL_STATE
                    or PhoneStateListener.LISTEN_CELL_LOCATION
                    or PhoneStateListener.LISTEN_SERVICE_STATE
                    or PhoneStateListener.LISTEN_SIGNAL_STRENGTH
                    or PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                    or PhoneStateListener.LISTEN_DATA_ACTIVITY
        )
    }

    private fun collectLocationStats(): String? {
        var currentKnownLocation: String? = null
        var lm = locationManager!!
            .getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (null == lm) {
            Log.i("Location: ", " NO LOCATION provided by GPS")
            lm = locationManager!!
                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }
        if (null == lm) {
            Log.i("Location: ", " NO LOCATION provided by Network")
        } else {
            currentKnownLocation = lm.toString()
            Log.i("Location: ", currentKnownLocation)
        }
        return currentKnownLocation
    }

    private fun collectWiFiStats(): String {
        var info = "WiFi: NOT Connected"
        val wifiInfo = wifiManager!!.connectionInfo
        wifiInfo.describeContents()
        val id = wifiInfo.networkId
        Log.i("WiFiInfo: ", wifiInfo.toString())
        if (-1 == id) {
            Log.i("WiFi: ", "NOT Connected")
        } else {
            Log.i("WiFi: ", "Network id = $id")
            info = wifiInfo.toString()
        }
        return info
    }

    private fun collectConnectivityManagerStats(): HashMap<String, String?> {
        val stats = HashMap()
        val connectivityManager = applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        val detailedState: DetailedState = networkInfo.detailedState
        Log.i("ConnectivityManager: ", detailedState.toString())
        stats["Network Detailed State"] = detailedState.toString()

/*		networkInfo = connectivityManager.getActiveNetworkInfo();
		Log.i("Connected state: ", networkInfo.getState().toString());
*/
        if (networkInfo.isConnected) {
            Log.i("Connected: ", networkInfo.toString())
            stats["Network Info"] = networkInfo.toString()
        }
        return stats
    }

    /**
     * @return
     *
     */
    private fun collectTelephonyManagerStats(): HashMap<String, String?> {
        val stats = HashMap()
        Log.i(
            "TelephonyManager: ",
            " ==> Collecting TelephonyManager stats <=="
        )

        stats["Subscriber Id"] = telephonyManager!!.subscriberId
        stats["Line1 Number"] = telephonyManager!!.line1Number
        val timenow = SimpleDateFormat.getDateTimeInstance().format(Date(System.currentTimeMillis()))
        stats["Time"] = timenow
        stats["Location"] = collectLocationStats()
        Log.i(
            "TelephonyManager: ", " device id => "
                    + telephonyManager!!.deviceId
        )
        stats["Device Id"] = telephonyManager!!.deviceId
        Log.i(
            "TelephonyManager: ", " device software version => "
                    + telephonyManager!!.deviceSoftwareVersion
        )
        stats["Device software version"] = telephonyManager!!.deviceSoftwareVersion

        stats.putAll(collectConnectivityManagerStats())

        stats["WiFi"] = collectWiFiStats()

//		todo: collect battery information synchronously for storing in db
        stats["Battery"] = batteryInfo

        Log.i(
            "TelephonyManager: ", " subscriber id => "
                    + telephonyManager!!.subscriberId
        )
        val nt = telephonyManager!!.networkType
        when (nt) {
            TelephonyManager.NETWORK_TYPE_EDGE -> {
                Log.i("TelephonyManager: ", " ==> EDGE Network <==")
                stats["Network type"] = "EDGE"
            }
            TelephonyManager.NETWORK_TYPE_GPRS -> {
                Log.i("TelephonyManager: ", " ==> GPRS Network <==")
                stats["Network type"] = "GPRS"
            }
            TelephonyManager.NETWORK_TYPE_UMTS -> {
                Log.i("TelephonyManager: ", " ==> UMTS Network <==")
                stats["Network type"] = "UMTS"
            }
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> {
                Log.i("TelephonyManager: ", " ==> Unknown Network <==")
                stats["Network type"] = "UNKNOWN"
            }
        }
        val pt = telephonyManager!!.phoneType
        when (pt) {
            TelephonyManager.PHONE_TYPE_GSM -> {
                Log.i("TelephonyManager: ", " ==> PHONE IS GSM TYPE <==")
                stats["Phone type"] = "GSM"
            }
            TelephonyManager.PHONE_TYPE_NONE ->
                Log.i("TelephonyManager: ", " ==> PHONE TYPE IS UNKNOWN <==")
        }
        val ss = telephonyManager!!.simState
        when (ss) {
            TelephonyManager.SIM_STATE_ABSENT -> {
                Log.i("TelephonyManager: ", " ==> SIM STATE ABSENT <==")
                stats["SIM State"] = "Absent"
            }
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> {
                Log.i("TelephonyManager: ", " ==> SIM STATE NETWORK LOCKED <==")
                stats["SIM State"] = "Network Locked"
            }
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> {
                Log.i("TelephonyManager: ", " ==> SIM STATE PIN REQUIRED <==")
                stats["SIM State"] = "PIN Required"
            }
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> {
                Log.i("TelephonyManager: ", " ==> SIM STATE PUK REQUIRED <==")
                stats["SIM State"] = "PUK Required"
            }
            TelephonyManager.SIM_STATE_READY -> {
                Log.i("TelephonyManager: ", " ==> SIM STATE READY <==")
                stats["SIM State"] = "Ready"
            }
            TelephonyManager.SIM_STATE_UNKNOWN -> {
                Log.i("TelephonyManager: ", " ==> SIM STATE UNKNOWN <==")
                stats["SIM State"] = "Unknown"
            }
        }
        val da = telephonyManager!!.dataActivity
        when (da) {
            TelephonyManager.DATA_ACTIVITY_IN ->
                Log.i("TelephonyManager: ", " ==> Data being Downloaded <==")
            TelephonyManager.DATA_ACTIVITY_OUT ->
                Log.i("TelephonyManager: ", " ==> Data being Uploaded <==")
            TelephonyManager.DATA_ACTIVITY_INOUT ->
                Log.i(
                    "TelephonyManager: ",
                    " ==> Data being Downloaded & Uploaded <=="
                )
            TelephonyManager.DATA_ACTIVITY_NONE ->
                Log.i("TelephonyManager: ", " ==> No Data Activity <==")
        }
        return stats
    }

    /**
     * @param serviceState
     * @return
     */
    private fun collectServiceStateStats(serviceState: ServiceState): HashMap<String, String?> {
        val stats = HashMap()
        Log.i("ServiceState: ", serviceState.toString())
        stats["ServiceState"] = serviceState.toString()
        val oplong = serviceState.operatorAlphaLong
        stats["Operator"] = oplong
        Log.i("ServiceState: ", oplong)
        Log.i("ServiceState: ", " is roaming => " + serviceState.roaming)
        stats["Roaming"] = java.lang.Boolean.toString(serviceState.roaming)
        Log.i(
            "ServiceState: ", " manual network selection => "
                    + serviceState.isManualSelection
        )
        stats["Manual Network selection"] = java.lang.Boolean.toString(serviceState.isManualSelection)
        val state = serviceState.state
        parseSignalState(stats, state)
        return stats
    }

    /**
     * @param stats
     * @param state
     */
    private fun parseSignalState(stats: HashMap<String, String?>, state: Int) {
//		signal strength can also be parsed for state
//		makes separate entry for signal strength here.. might not apply for state
//		todo: check if this is correct
        stats["Signal Strength"] = state.toString() + "asu"
        when (state) {
            ServiceState.STATE_IN_SERVICE -> {
                Log.i("ServiceState: ", " ==> IN SERVICE <==")
                stats["State"] = "IN SERVICE"
            }
            ServiceState.STATE_OUT_OF_SERVICE -> {
                Log.i("ServiceState: ", " ==> OUT OF SERVICE <==")
                stats["State"] = "OUT OF SERVICE"
            }
            ServiceState.STATE_EMERGENCY_ONLY -> {
                Log.i("ServiceState: ", " ==> EMERGENCY ONLY <==")
                stats["State"] = "EMERGENCY ONLY"
            }
            ServiceState.STATE_POWER_OFF -> {
                Log.i("ServiceState: ", " ==> POWER OFF <==")
                stats["State"] = "POWER OFF"
            }
        }
    }

    /**
     * @param serviceState
     */
    private fun storeServiceStateChange(serviceState: ServiceState) {
        Log.i(
            "IRService#storeServiceStateChange: ",
            " ==> Collecting ServiceState stats <=="
        )

        val stats = HashMap()
        stats["Event"] = "Service State Change"
        stats.putAll(collectTelephonyManagerStats())
        stats.putAll(collectServiceStateStats(serviceState))
        storeStatsInDB(stats)
    }

    private fun storeDataConnectionStateChange(reason: String) {
        val stats = HashMap()
        stats["Event"] = "DataConnection State Change"
        stats["Data Connection"] = reason
        stats.putAll(collectTelephonyManagerStats())
        storeStatsInDB(stats)
    }

    /**
     * @param asu
     */
    private fun storeSignalStrengthChange(asu: Int) {
        val stats = HashMap()
        stats["Event"] = "Signal Strength Change"
        parseSignalState(stats, asu)
        stats.putAll(collectTelephonyManagerStats())
        storeStatsInDB(stats)
    }

    private fun storeCallStateChange(reason: String) {
        val stats = HashMap()
        stats["Event"] = "Call State Change"
        stats["Call State"] = reason
        stats.putAll(collectTelephonyManagerStats())
        storeStatsInDB(stats)
    }

    /**
     * @param stats
     */
    private fun storeStatsInDB(stats: HashMap<String, String?>) {
        val serviceStateDetails = JSONObject(stats as Map<*, *>)

        val contentValues = ContentValues()
        contentValues.put(IncidentReport.Details.ID, stats["Subscriber Id"])
        try {
            contentValues.put(IncidentReport.Details.DETAILS, serviceStateDetails.toString(2))
            val uri = contentResolver.insert(IncidentReport.Details.CONTENT_URI, contentValues)
            Log.d("IRService#storeStatsInDB: ", uri.toString())
        } catch (e: JSONException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            Log.e(TAG, "IRService#storeStatsInDB: failed to extract JSON data")
        }
//		also uploads to server - realtime
//		todo: change this to timed update
        uploadStatsToServer(stats)
    }

    private fun uploadStatsToServer(stats: HashMap<String, String?>) {
        val httpHandler = HttpHandler()
        val nvps: MutableList<NameValuePair> = ArrayList()
        try {
            val serviceStateDetails = JSONObject(stats as Map<*, *>)
            nvps.add(BasicNameValuePair("content", serviceStateDetails.toString(2)))
            val response = httpHandler.post("http://ibt.appspot.com/upload", nvps)
            Log.i(TAG, ">$response<")
            if ("OK".equals(response.trim { it <= ' ' }, ignoreCase = true)) {
                Log.i(TAG, "Successfully Uploaded to Server")
            } else {
                Log.e(TAG, "Failed to Upload to Server")
            }
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
        } catch (e: JSONException) {
            Log.e(TAG, e.message, e)
        }
    }

    companion object {
        private const val TAG = "IRService"
    }
}
