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
     * The Essential list: blukit works on Bluetooth.
     */
    val essentialPermissions: List<String> = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /**
     * The Optional list: blukit uses WiFi and additional Location if granted.
     */
    val optionalPermissions: List<String> = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        add(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        if (Build.VERSION.SDK_INT >= 35) {
            add("android.permission.ACCESS_LOCAL_NETWORK")
        }
    }

    /**
     * All permissions that the app can request.
     */
    val requiredPermissions: List<String> = essentialPermissions + optionalPermissions

    private val _permissionsGranted = MutableStateFlow(checkEssentialGranted())
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    /**
     * Checks if all required permissions (essential + optional) are healthy.
     */
    fun checkAllGranted(): Boolean = checkListGranted(requiredPermissions)

    /**
     * Checks if essential permissions (Bluetooth) are healthy.
     */
    fun checkEssentialGranted(): Boolean = checkListGranted(essentialPermissions)

    private fun checkListGranted(permissions: List<String>): Boolean {
        return permissions.all { permission ->
            val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            val isRuntime = isRuntimePermission(permission)
            if (!isGranted && !isRuntime) return@all true 
            isGranted
        }
    }

    /**
     * Updates the internal flow state. Call this after a permission request result.
     */
    fun refresh() {
        _permissionsGranted.value = checkEssentialGranted()
    }

    private fun isRuntimePermission(permission: String): Boolean {
        return try {
            val info = context.packageManager.getPermissionInfo(permission, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.protection == android.content.pm.PermissionInfo.PROTECTION_DANGEROUS
            } else {
                @Suppress("DEPRECATION")
                (info.protectionLevel and android.content.pm.PermissionInfo.PROTECTION_DANGEROUS) != 0
            }
        } catch (_: Exception) {
            false
        }
    }
}
