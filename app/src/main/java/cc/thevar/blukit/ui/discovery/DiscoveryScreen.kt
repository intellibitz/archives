package cc.thevar.blukit.ui.discovery

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.P2PDevice
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (P2PDevice) -> Unit,
    onStartServer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Comprehensive permission list for Nearby Connections
    val permissions = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        // Always request location for maximum compatibility with Nearby Connections API on real hardware
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            onStartScan()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.discovery_title)) },
                actions = {
                    IconButton(onClick = onStartServer) {
                        Icon(Icons.Rounded.Bluetooth, contentDescription = stringResource(R.string.discovery_start_server))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (!state.isBluetoothEnabled || !state.isLocationEnabled) {
                RadioStateWarning(
                    isBluetoothEnabled = state.isBluetoothEnabled,
                    isLocationEnabled = state.isLocationEnabled,
                    onEnableBluetooth = {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    },
                    onEnableLocation = {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                )
            }

            if (!permissionState.allPermissionsGranted) {
                PermissionRequestContent(
                    onRequestPermissions = { permissionState.launchMultiplePermissionRequest() }
                )
            } else {
                RadarScreen(
                    state = state,
                    onDeviceClick = onDeviceClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // Error display
            state.errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(text = error)
                }
            }
        }
    }
}

@Composable
private fun RadioStateWarning(
    isBluetoothEnabled: Boolean,
    isLocationEnabled: Boolean,
    onEnableBluetooth: () -> Unit,
    onEnableLocation: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Warning, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                val message = when {
                    !isBluetoothEnabled && !isLocationEnabled -> stringResource(R.string.radio_warning_both)
                    !isBluetoothEnabled -> stringResource(R.string.radio_warning_bluetooth)
                    else -> stringResource(R.string.radio_warning_location)
                }
                Text(text = message, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = stringResource(R.string.radio_warning_required), style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = if (!isBluetoothEnabled) onEnableBluetooth else onEnableLocation) {
                Text(stringResource(R.string.radio_enable_btn))
            }
        }
    }
}

@Composable
private fun PermissionRequestContent(
    onRequestPermissions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.permission_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.permission_desc),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermissions) {
            Text(stringResource(R.string.permission_grant))
        }
    }
}
