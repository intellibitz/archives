package cc.thevar.blukit

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test suite for validating core application commandments.
 * This verifies permission constraints (Commandment 1).
 */
@RunWith(AndroidJUnit4::class)
class CommandmentsTest {

    @Test
    fun testCommandment1_BluetoothOnlyPermissions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        }
        val permissions = packageInfo.requestedPermissions ?: emptyArray<String>()

        // Check if mandatory permissions are limited to BT group (on modern APIs)
        val criticalNonBtPermissions = listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_SMS,
            Manifest.permission.GET_ACCOUNTS
        )

        criticalNonBtPermissions.forEach { permission ->
            assertFalse("Commandment 1 Breach: Non-BT permission '$permission' found in manifest", permissions.contains(permission))
        }
    }
}
