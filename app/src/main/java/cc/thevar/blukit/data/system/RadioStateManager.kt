package cc.thevar.blukit.data.system

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.*

/**
 * THE RADIO CORE.
 * The unbreakable monitor for hardware radio states.
 */
data class RadioStates(
    val isBluetoothEnabled: Boolean,
    val isLocationEnabled: Boolean,
    val isWifiEnabled: Boolean
)

class RadioStateManager(private val context: Context) {

    private val tag = "BlukitRadio"
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _radioStates = MutableStateFlow(getCurrentStates())
    val radioStates: StateFlow<RadioStates> = _radioStates.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val newState = getCurrentStates()
            Log.d(tag, "Radio Hardware Shift: BT=${newState.isBluetoothEnabled}, GPS=${newState.isLocationEnabled}, WiFi=${newState.isWifiEnabled}")
            _radioStates.value = newState
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                addAction(LocationManager.MODE_CHANGED_ACTION)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
    }

    /**
     * Force a fresh check of all hardware radios.
     */
    fun triggerRefresh() {
        _radioStates.value = getCurrentStates()
    }

    /**
     * Deep hardware interrogation.
     * Handles device-specific quirks like SecurityExceptions or stale adapter states.
     */
    fun getCurrentStates(): RadioStates {
        val adapter = bluetoothManager?.adapter
        
        // INTERROGATION: Bluetooth
        val isBtOn = try {
            adapter?.isEnabled == true || adapter?.state == BluetoothAdapter.STATE_ON
        } catch (e: SecurityException) {
            Log.w(tag, "Quirk: BT SecurityException. Defaulting to false.")
            false
        }

        // INTERROGATION: Location (Required for BLE/Nearby discovery on most Androids)
        val isGpsOn = try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            Log.w(tag, "Quirk: Location interrogation failed.")
            false
        }

        // INTERROGATION: WiFi
        val isWifiOn = try {
            wifiManager.isWifiEnabled
        } catch (e: Exception) {
            // Resilience: If WiFi interrogation fails, we assume it shouldn't block the air.
            true 
        }

        // Hardened logic: On Android 12+, Location isn't strictly mandatory for "nearby" if 
        // neverForLocation is used, but we keep it for maximum fidelity across the fleet.
        val locationIsMandatory = Build.VERSION.SDK_INT < Build.VERSION_CODES.S

        return RadioStates(
            isBluetoothEnabled = isBtOn,
            isLocationEnabled = if (locationIsMandatory) isGpsOn else true, // GPS only blocks old phones
            isWifiEnabled = isWifiOn
        )
    }
}
