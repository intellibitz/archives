package cc.thevar.acc

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.acc.ui.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val agentVm: AgentViewModel = koinViewModel()
    val systemVm: SystemViewModel = koinViewModel()
    val consoleVm: ConsoleViewModel = koinViewModel()
    
    val messages by agentVm.messages.collectAsState()
    val systemState by systemVm.state.collectAsState()
    val consoleLines by consoleVm.lines.collectAsState()

    val authState by AuthStore.credentials.collectAsState()
    var showLogin by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState != null) {
            agentVm.connect()
            systemVm.connect()
            consoleVm.connect()
        } else {
            showLogin = true
        }
    }

    AccTheme {
        if (showLogin) {
            LoginDialog(onLogin = { user, pass ->
                AuthStore.setCredentials(user, pass)
                showLogin = false
            })
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("AI Command Center [acc]", fontWeight = FontWeight.Bold) },
                    actions = {
                        Text(systemState?.statusMsg ?: "Connecting...", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(if (systemState?.proxyOnline == true) "PROXY ON" else "PROXY OFF", 
                            if (systemState?.proxyOnline == true) Color.Green else Color.Gray)
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing // Proper Edge-to-Edge support
        ) { padding ->
            BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
                val isCompact = maxWidth < 800.dp
                
                if (isCompact) {
                    // Mobile Layout: Sidebar hidden, maybe accessible via Drawer or Bottom Sheet
                    // For now, let's just show a condensed view
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp)) {
                            ThoughtStream(messages)
                        }
                        Box(modifier = Modifier.height(200.dp).fillMaxWidth().padding(8.dp)) {
                            ConsoleView(consoleLines)
                        }
                    }
                } else {
                    // Desktop Layout: Sidebar + Main + System Panel
                    Row(modifier = Modifier.fillMaxSize()) {
                        Sidebar(
                            systemState = systemState,
                            onCommand = { consoleVm.runCommand(it) },
                            onUpdate = { systemVm.updateSystem() }
                        )
                        
                        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Row(modifier = Modifier.weight(0.7f).fillMaxWidth()) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(8.dp)) {
                                    ThoughtStream(messages)
                                }
                                Box(modifier = Modifier.width(300.dp).fillMaxHeight().padding(8.dp)) {
                                    SystemPanel(
                                        systemState, 
                                        onCommand = { consoleVm.runCommand(it) }, 
                                        onSpawn = { name, model -> systemVm.spawnAgent(name, model) },
                                        onStartEngine = { provider -> systemVm.startEngine(provider) }
                                    )
                                }
                            }
                            
                            Box(modifier = Modifier.weight(0.3f).fillMaxWidth().padding(8.dp)) {
                                ConsoleView(consoleLines)
                            }
                        }
                    }
                }
            }
        }
    }
}
