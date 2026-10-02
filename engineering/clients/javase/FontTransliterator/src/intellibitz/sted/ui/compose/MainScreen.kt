package intellibitz.sted.ui.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import intellibitz.sted.fontmap.FontMap
import java.io.File

@Composable
fun MainScreen(appState: AppState) {
    // Add an initial empty FontMap if none open
    LaunchedEffect(Unit) {
        if (appState.openTabs.isEmpty()) {
            appState.newFontMap()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("FontTransliterator") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // Tabs Row
            if (appState.openTabs.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = appState.selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    appState.openTabs.forEachIndexed { index, fontMap ->
                        Tab(
                            selected = appState.selectedTabIndex == index,
                            onClick = { appState.selectedTabIndex = index },
                            text = { 
                                val title = if (fontMap.isNew()) "Untitled" else fontMap.getFileName().substringAfterLast(File.separator)
                                Text(title + if (fontMap.isDirty) " *" else "")
                            }
                        )
                    }
                }
                
                // Content for selected tab
                val currentFontMap = appState.currentFontMap
                if (currentFontMap != null) {
                    MappingEditor(fontMap = currentFontMap)
                }
            } else {
                Text("No open mappings. Create a new one.")
            }
        }
    }
}
