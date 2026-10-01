package intellibitz.sted

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.MenuBar
import androidx.compose.runtime.remember
import intellibitz.sted.ui.compose.AppState

@Composable
@Preview
fun MainApp() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center) {
                Text("Welcome to FontTransliterator (Compose)")
            }
        }
    }
}

fun main() = application {
    val appState = remember { AppState() }
    
    Window(onCloseRequest = ::exitApplication, title = "FontTransliterator") {
        MenuBar {
            Menu("File") {
                Item("New", onClick = { appState.newFontMap() })
                Item("Open...", onClick = { appState.openFontMap() })
                Item("Save", onClick = { appState.saveFontMap() })
                Item("Save As...", onClick = { /* TODO */ })
                Separator()
                Item("Exit", onClick = ::exitApplication)
            }
            Menu("Action") {
                Item("Transliterate File...", onClick = { appState.transliterateFile() })
            }
        }
        intellibitz.sted.ui.compose.MainScreen(appState)
    }
}
