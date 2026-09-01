package cc.thevar.blukit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Stream
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.screens.SyncProgressIndicator
import cc.thevar.blukit.ui.theme.StealthBlack
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BlukitScaffold(
    currentRoute: Route,
    title: String,
    nickname: String?,
    syncProgress: Float?,
    snackbarHostState: SnackbarHostState,
    onNavigate: (Route) -> Unit,
    onLogout: () -> Unit,
    onResetProfile: () -> Unit,
    onBack: (() -> Unit)?,
    connectionStatus: String,
    trend: String?,
    isBluetoothEnabled: Boolean,
    isWifiEnabled: Boolean,
    onAwakenBluetooth: () -> Unit,
    onAwakenWifi: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val showNavigation = nickname != null && currentRoute !is Route.Onboarding

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            if (showNavigation) {
                listOf(
                    Triple(Route.Nearby, "Nearby", Icons.Rounded.Radar),
                    Triple(Route.LiveFeed, "Live", Icons.Rounded.Stream),
                    Triple(Route.Timeline, "Messages", Icons.Rounded.History),
                ).forEach { (route, label, icon) ->
                    item(
                        selected = currentRoute::class == route::class,
                        onClick = { onNavigate(route) },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = StealthBlack,
        contentColor = Color.White
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (currentRoute !is Route.Onboarding) {
                    Column {
                        BlukitToolbar(
                            title = title,
                            onLogout = onLogout,
                            onResetProfile = onResetProfile,
                            themeColor = if (currentRoute is Route.GroupField) StealthRose else StealthPrimary,
                            onBack = onBack,
                            connectionStatus = connectionStatus,
                            trend = trend,
                            isBluetoothOff = !isBluetoothEnabled,
                            isWifiOff = !isWifiEnabled,
                            onAwakenBluetooth = onAwakenBluetooth,
                            onAwakenWifi = onAwakenWifi
                        )
                        SyncProgressIndicator(progress = syncProgress)
                    }
                }
            },
            content = content
        )
    }
}
