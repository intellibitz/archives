package cc.thevar.blukit.data.system

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

import android.util.Log

data class RadioStates(
    val isBluetoothEnabled: Boolean,
    val isLocationEnabled: Boolean,
)

/**
 * Hardened Radio State Manager.
 * Monitors Bluetooth and Location states reactively.
 */
class RadioStateManager(private val context: Context) {

    private val tag = "BlukitRadio"
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _radioStates = MutableStateFlow(getCurrentStates())
    val radioStates: StateFlow<RadioStates> = _radioStates.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val newState = getCurrentStates()
            Log.d(tag, "Radio state changed: BT=${newState.isBluetoothEnabled}, GPS=${newState.isLocationEnabled}")
            _radioStates.value = newState
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
    }

    fun triggerRefresh() {
        val newState = getCurrentStates()
        Log.d(tag, "Manual refresh: BT=${newState.isBluetoothEnabled}")
        _radioStates.value = newState
    }

    fun getCurrentStates(): RadioStates {
        val adapter = bluetoothManager?.adapter
        
        val hasConnectPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        // On some devices, even if physically on, isEnabled might return false if 
        // other related permissions (like SCAN) are missing or if the adapter is in a weird state.
        val isBtEnabled = try {
            adapter?.isEnabled == true
        } catch (e: SecurityException) {
            Log.w(tag, "SecurityException checking BT state: ${e.message}")
            false
        }

        val isLocationEnabled = try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            Log.w(tag, "Exception checking Location state: ${e.message}")
            false
        }

        // Location is an optional breeze on Android 12+, but mandatory for the Air on older devices
        val isLocationMandatory = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
        Log.d(tag, "Vibe Check: Bluetooth=$isBtEnabled (Required), GPS=$isLocationEnabled (Optional=${!isLocationMandatory}), Wi-Fi=Optional")

        return RadioStates(
            isBluetoothEnabled = isBtEnabled && hasConnectPermission,
            isLocationEnabled = if (isLocationMandatory) isLocationEnabled else true // Suppress "Disabled" UI on 12+ if location is off
        )
    }
}
