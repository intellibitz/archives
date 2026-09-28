package intellibitz.sted.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.FontMapEntry

@Composable
fun MappingEditor(fontMap: FontMap) {
    // In a real reactive app, we should observe FontMap changes
    // But since FontMap is a legacy object, we trigger recomposition when we mutate it
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // State for new entry
    var newFrom by remember { mutableStateOf("") }
    var newTo by remember { mutableStateOf("") }
    var newBeginsWith by remember { mutableStateOf(false) }
    var newEndsWith by remember { mutableStateOf(false) }
    var newFollowedBy by remember { mutableStateOf("") }
    var newPrecededBy by remember { mutableStateOf("") }
    var newConditional by remember { mutableStateOf("AND") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Top section: Fonts configuration
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = fontMap.font1Path,
                onValueChange = { fontMap.font1Path = it; refreshTrigger++ },
                label = { Text("Map File From (Font 1)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = fontMap.font2Path,
                onValueChange = { fontMap.font2Path = it; refreshTrigger++ },
                label = { Text("Map File To (Font 2)") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Table Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("From", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("To", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("Begins", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold)
            Text("Ends", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold)
            Text("Preceded", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("Followed", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("Cond", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(48.dp)) // For delete button
        }
        
        Divider()
        
        // Table Content
        val entries = fontMap.entries.values().toList()
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(entries) { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(entry.from, modifier = Modifier.weight(1f))
                    Text(entry.to, modifier = Modifier.weight(1f))
                    Text(entry.beginsWith.toString(), modifier = Modifier.weight(0.5f))
                    Text(entry.endsWith.toString(), modifier = Modifier.weight(0.5f))
                    Text(entry.precededBy ?: "", modifier = Modifier.weight(1f))
                    Text(entry.followedBy ?: "", modifier = Modifier.weight(1f))
                    Text(entry.conditional, modifier = Modifier.weight(0.8f))
                    IconButton(onClick = {
                        fontMap.entries.remove(entry)
                        fontMap.isDirty = true
                        refreshTrigger++
                    }, modifier = Modifier.width(48.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
                Divider()
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Add New Entry Form
        Text("Add New Entry", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newFrom,
                onValueChange = { newFrom = it },
                label = { Text("From") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = newTo,
                onValueChange = { newTo = it },
                label = { Text("To") },
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(0.5f)) {
                Checkbox(checked = newBeginsWith, onCheckedChange = { newBeginsWith = it })
                Text("Begins", style = MaterialTheme.typography.caption)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(0.5f)) {
                Checkbox(checked = newEndsWith, onCheckedChange = { newEndsWith = it })
                Text("Ends", style = MaterialTheme.typography.caption)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newPrecededBy,
                onValueChange = { newPrecededBy = it },
                label = { Text("Preceded By") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = newFollowedBy,
                onValueChange = { newFollowedBy = it },
                label = { Text("Followed By") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = newConditional,
                onValueChange = { newConditional = it },
                label = { Text("Cond (AND/OR/NOT)") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (newFrom.isNotBlank() && newTo.isNotBlank()) {
                        val entry = FontMapEntry(from = newFrom, to = newTo).apply {
                            beginsWith = newBeginsWith
                            endsWith = newEndsWith
                            precededBy = newPrecededBy.takeIf { it.isNotBlank() }
                            followedBy = newFollowedBy.takeIf { it.isNotBlank() }
                            conditional = newConditional
                        }
                        fontMap.entries.add(entry)
                        fontMap.isDirty = true
                        refreshTrigger++ // Force UI update
                        
                        // Reset form
                        newFrom = ""
                        newTo = ""
                        newBeginsWith = false
                        newEndsWith = false
                        newPrecededBy = ""
                        newFollowedBy = ""
                        newConditional = "AND"
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(Modifier.width(4.dp))
                Text("Add")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Test Section
        Text("Test Mapping", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold)
        var testInput by remember { mutableStateOf("") }
        var testOutput by remember { mutableStateOf("") }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = testInput,
                onValueChange = { 
                    testInput = it
                    val transliterator = intellibitz.sted.fontmap.DefaultTransliterator()
                    transliterator.setEntries(fontMap.entries)
                    testOutput = transliterator.parseLine(it) ?: ""
                },
                label = { Text("Input Text") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = testOutput,
                onValueChange = {},
                readOnly = true,
                label = { Text("Transliterated Output") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

