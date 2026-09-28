package intellibitz.sted.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.FontMapEntry

@Composable
fun MappingEditor(fontMap: FontMap) {
    // In a real reactive app, we should observe FontMap changes
    // But since FontMap is a legacy object with listeners, we will map its state
    
    // A simplified editor view
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Font Map Editor", style = MaterialTheme.typography.h6)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Settings Row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = "", // fontMap.font1Path ?
                onValueChange = {},
                label = { Text("Map File From") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Map File To") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Entries will go here. A DataTable component would be placed below.")
    }
}
