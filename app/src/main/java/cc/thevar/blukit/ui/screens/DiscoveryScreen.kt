package cc.thevar.blukit.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
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
    onNavigateToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Commandment 1: Purely Bluetooth only permissions
    val permissions = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            // Pre-S versions technically need Location for BT scanning, 
            // but we follow the commandment to minimize to BT only.
            // If the SDK requires it, Nearby API will handle the error internally.
        }
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    DisposableEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            onStartScan()
        }
        onDispose { }
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
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = state.isConnected,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToLobby,
                    icon = { Icon(Icons.Rounded.Campaign, contentDescription = null) },
                    text = { Text("📢 Lobby") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (!permissionState.allPermissionsGranted) {
                PermissionRequestContent(
                    onRequestPermissions = { permissionState.launchMultiplePermissionRequest() },
                    modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
                )
            } else {
                RadarScreen(
                    state = state,
                    onDeviceClick = onDeviceClick,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Debug/Status Overlay - Commandment 1: BT focused
                StatusOverlay(
                    isDiscovering = state.isDiscovering,
                    isBluetoothEnabled = state.isBluetoothEnabled,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(innerPadding)
                        .padding(16.dp)
                )

                // Overlay warnings/errors on top of Radar
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                        .fillMaxWidth()
                ) {
                    // Commandment 2 & 3: Location/Wifi are optional. Warning only for Bluetooth.
                    if (!state.isBluetoothEnabled) {
                        RadioStateWarning(
                            onEnableBluetooth = {
                                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                            }
                        )
                    }

                    // Error display (Nearby API might still report 8032 if it really wants WiFi, 
                    // but we stay true to commandments and let the user decide)
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
    }
}

@Composable
private fun StatusOverlay(
    isDiscovering: Boolean,
    isBluetoothEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.5f),
        contentColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            StatusItem("Radar", isDiscovering)
            StatusItem("BT", isBluetoothEnabled)
        }
    }
}

@Composable
private fun StatusItem(label: String, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (active) Color.Green else Color.Red)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RadioStateWarning(
    onEnableBluetooth: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Warning, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.radio_warning_bluetooth), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = "Bluetooth is required for offline discovery.", style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = onEnableBluetooth) {
                Text(stringResource(R.string.radio_enable_btn))
            }
        }
    }
}

@Composable
private fun PermissionRequestContent(
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bluetooth Permission Required",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Blukit uses Bluetooth to find and connect to nearby friends without using any internet.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermissions) {
            Text("Grant Bluetooth Access")
        }
    }
}
