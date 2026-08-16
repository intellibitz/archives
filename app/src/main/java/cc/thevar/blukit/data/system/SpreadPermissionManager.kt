package cc.thevar.blukit.data.system

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * THE SPREAD PERMISSION CORE.
 * The unbreakable source of truth for proximity permissions.
 */
class SpreadPermissionManager(private val context: Context) {

    /**
     * The essential list of permissions required for the sentient field to function.
     * Adapts automatically based on the device's SDK level.
     */
    val requiredPermissions: List<String> = buildList {
        // Bluetooth permissions (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        
        // Location is mandatory for Nearby Connections / BLE Scanning on all versions
        // but the usage differs. Fine Location is the "Gold Standard".
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)

        // WiFi Proximity (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        // Local Network (Android 15+) - Handle as optional/resilient
        if (Build.VERSION.SDK_INT >= 35) {
            add("android.permission.ACCESS_LOCAL_NETWORK")
        }
    }

    private val _permissionsGranted = MutableStateFlow(checkAllGranted())
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    /**
     * Checks if all required permissions are currently healthy.
     */
    fun checkAllGranted(): Boolean {
        return requiredPermissions.all { permission ->
            val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            
            // Resilience: If a permission isn't considered "Runtime" on this device,
            // we don't let it block the core logic.
            if (!isGranted && !isRuntimePermission(permission)) {
                return@all true 
            }
            
            isGranted
        }
    }

    /**
     * Updates the internal flow state. Call this after a permission request result.
     */
    fun refresh() {
        _permissionsGranted.value = checkAllGranted()
    }

    private fun isRuntimePermission(permission: String): Boolean {
        return try {
            val info = context.packageManager.getPermissionInfo(permission, 0)
            (info.protectionLevel and android.content.pm.PermissionInfo.PROTECTION_DANGEROUS) != 0
        } catch (e: Exception) {
            false
        }
    }
}
