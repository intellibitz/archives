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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.platform.asComposeFontFamily
import androidx.compose.ui.unit.dp
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.FontMapEntry
import java.io.File

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
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
    
    // State for filtering
    var hideUnusable by remember { mutableStateOf(false) }
    
    // Load custom fonts using Compose AWT interop
    val font1Family = remember(fontMap.font1, fontMap.font1Path, refreshTrigger) {
        fontMap.font1?.asComposeFontFamily()
    }
    val font2Family = remember(fontMap.font2, fontMap.font2Path, refreshTrigger) {
        fontMap.font2?.asComposeFontFamily()
    }
    
    // State for System Font Dialogs
    var showFont1Dialog by remember { mutableStateOf(false) }
    var showFont2Dialog by remember { mutableStateOf(false) }
    
    var font1SearchQuery by remember { mutableStateOf("") }
    var font2SearchQuery by remember { mutableStateOf("") }

    if (showFont1Dialog) {
        AlertDialog(
            onDismissRequest = { showFont1Dialog = false },
            title = { Text("Select System Font 1") },
            text = {
                Column {
                    OutlinedTextField(
                        value = font1SearchQuery,
                        onValueChange = { font1SearchQuery = it },
                        label = { Text("Search Fonts (e.g. Latha, Arial)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    val allFonts = intellibitz.sted.util.Resources.fonts.keys.toList().sorted()
                    val filteredFonts = allFonts.filter { it.contains(font1SearchQuery, ignoreCase = true) }
                    LazyColumn(modifier = Modifier.weight(1f, fill = false).heightIn(max = 300.dp)) {
                        items(filteredFonts) { fontName ->
                            TextButton(onClick = { 
                                fontMap.font1Path = intellibitz.sted.util.Resources.SYSTEM
                                fontMap.setFont1(fontName)
                                refreshTrigger++
                                showFont1Dialog = false
                            }) {
                                Text(fontName)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFont1Dialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showFont2Dialog) {
        AlertDialog(
            onDismissRequest = { showFont2Dialog = false },
            title = { Text("Select System Font 2") },
            text = {
                Column {
                    OutlinedTextField(
                        value = font2SearchQuery,
                        onValueChange = { font2SearchQuery = it },
                        label = { Text("Search Fonts (e.g. Latha, Arial)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    val allFonts = intellibitz.sted.util.Resources.fonts.keys.toList().sorted()
                    val filteredFonts = allFonts.filter { it.contains(font2SearchQuery, ignoreCase = true) }
                    LazyColumn(modifier = Modifier.weight(1f, fill = false).heightIn(max = 300.dp)) {
                        items(filteredFonts) { fontName ->
                            TextButton(onClick = { 
                                fontMap.font2Path = intellibitz.sted.util.Resources.SYSTEM
                                fontMap.setFont2(fontName)
                                refreshTrigger++
                                showFont2Dialog = false
                            }) {
                                Text(fontName)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFont2Dialog = false }) { Text("Cancel") }
            }
        )
    }

    var currentTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TabRow(selectedTabIndex = currentTab) {
            Tab(selected = currentTab == 0, onClick = { currentTab = 0 }, text = { Text("Mapping Rules") })
            Tab(selected = currentTab == 1, onClick = { currentTab = 1 }, text = { Text("Transliterate Text") })
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Top section: Fonts configuration (Shared across both tabs)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = if (fontMap.font1Path == intellibitz.sted.util.Resources.SYSTEM) fontMap.font1?.name ?: "" else fontMap.font1Path,
                onValueChange = { fontMap.font1Path = it; refreshTrigger++ },
                label = { Text("Map File From (Font 1)") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Select Font 1", java.awt.FileDialog.LOAD)
                dialog.isVisible = true
                if (dialog.file != null) {
                    val file = File(dialog.directory, dialog.file)
                    fontMap.font1Path = file.absolutePath
                    fontMap.setFont1(file)
                    refreshTrigger++
                }
            }) { Text("Browse") }
            Button(onClick = { showFont1Dialog = true }) { Text("System") }
            
            OutlinedTextField(
                value = if (fontMap.font2Path == intellibitz.sted.util.Resources.SYSTEM) fontMap.font2?.name ?: "" else fontMap.font2Path,
                onValueChange = { fontMap.font2Path = it; refreshTrigger++ },
                label = { Text("Map File To (Font 2)") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Select Font 2", java.awt.FileDialog.LOAD)
                dialog.isVisible = true
                if (dialog.file != null) {
                    val file = File(dialog.directory, dialog.file)
                    fontMap.font2Path = file.absolutePath
                    fontMap.setFont2(file)
                    refreshTrigger++
                }
            }) { Text("Browse") }
            Button(onClick = { showFont2Dialog = true }) { Text("System") }
        }
        
        // Shared Options
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = hideUnusable, onCheckedChange = { hideUnusable = it })
            Text("Hide Unusable Characters (based on selected fonts)")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (currentTab == 0) {
            // Mapping Rules Tab
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
        val entries = fontMap.entries.values().toList().filter { entry ->
            if (!hideUnusable) true
            else {
                val canDisplayFrom = fontMap.font1?.let { it.canDisplayUpTo(entry.from) == -1 } ?: true
                val canDisplayTo = fontMap.font2?.let { it.canDisplayUpTo(entry.to) == -1 } ?: true
                canDisplayFrom && canDisplayTo
            }
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(entries) { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(entry.from, modifier = Modifier.weight(1f), fontFamily = font1Family)
                    Text(entry.to, modifier = Modifier.weight(1f), fontFamily = font2Family)
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
        
        // Keypad Section
        Row(modifier = Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Font 1 Keypad
            Column(modifier = Modifier.weight(1f)) {
                Text("Font 1 Keypad", style = MaterialTheme.typography.subtitle2, fontWeight = FontWeight.Bold)
                if (fontMap.font1 != null && font1Family != null) {
                    val font1Blocks = remember(fontMap.font1) {
                        val blocks = mutableSetOf<String>()
                        for (i in 32..0xFFFF) {
                            if (fontMap.font1!!.canDisplay(i)) {
                                val block = Character.UnicodeBlock.of(i)
                                if (block != null) blocks.add(block.toString())
                            }
                        }
                        blocks.toList().sorted()
                    }
                    var selectedBlock by remember(font1Blocks) { mutableStateOf(font1Blocks.firstOrNull() ?: "") }
                    var expanded by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.padding(top = 4.dp)) {
                        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (selectedBlock.isEmpty()) "Select Block" else selectedBlock, maxLines = 1)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            font1Blocks.forEach { block ->
                                DropdownMenuItem(onClick = {
                                    selectedBlock = block
                                    expanded = false
                                }) {
                                    Text(block)
                                }
                            }
                        }
                    }

                    val font1Chars = remember(fontMap.font1, selectedBlock) {
                        val list = mutableListOf<String>()
                        for (i in 32..0xFFFF) {
                            if (fontMap.font1!!.canDisplay(i)) {
                                val block = Character.UnicodeBlock.of(i)
                                if (block != null && block.toString() == selectedBlock) {
                                    list.add(i.toChar().toString())
                                }
                            }
                        }
                        list
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(32.dp),
                        modifier = Modifier.fillMaxSize().padding(top = 4.dp)
                    ) {
                        items(font1Chars) { charStr ->
                            TextButton(
                                onClick = { newFrom += charStr },
                                modifier = Modifier.size(32.dp).padding(2.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(charStr, fontFamily = font1Family, color = MaterialTheme.colors.onSurface)
                            }
                        }
                    }
                } else {
                    Text("No Font 1 Selected", modifier = Modifier.padding(top=4.dp))
                }
            }

            // Font 2 Keypad
            Column(modifier = Modifier.weight(1f)) {
                Text("Font 2 Keypad", style = MaterialTheme.typography.subtitle2, fontWeight = FontWeight.Bold)
                if (fontMap.font2 != null && font2Family != null) {
                    val font2Blocks = remember(fontMap.font2) {
                        val blocks = mutableSetOf<String>()
                        for (i in 32..0xFFFF) {
                            if (fontMap.font2!!.canDisplay(i)) {
                                val block = Character.UnicodeBlock.of(i)
                                if (block != null) blocks.add(block.toString())
                            }
                        }
                        blocks.toList().sorted()
                    }
                    var selectedBlock by remember(font2Blocks) { mutableStateOf(font2Blocks.firstOrNull() ?: "") }
                    var expanded by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.padding(top = 4.dp)) {
                        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (selectedBlock.isEmpty()) "Select Block" else selectedBlock, maxLines = 1)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            font2Blocks.forEach { block ->
                                DropdownMenuItem(onClick = {
                                    selectedBlock = block
                                    expanded = false
                                }) {
                                    Text(block)
                                }
                            }
                        }
                    }

                    val font2Chars = remember(fontMap.font2, selectedBlock) {
                        val list = mutableListOf<String>()
                        for (i in 32..0xFFFF) {
                            if (fontMap.font2!!.canDisplay(i)) {
                                val block = Character.UnicodeBlock.of(i)
                                if (block != null && block.toString() == selectedBlock) {
                                    list.add(i.toChar().toString())
                                }
                            }
                        }
                        list
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(32.dp),
                        modifier = Modifier.fillMaxSize().padding(top = 4.dp)
                    ) {
                        items(font2Chars) { charStr ->
                            TextButton(
                                onClick = { newTo += charStr },
                                modifier = Modifier.size(32.dp).padding(2.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(charStr, fontFamily = font2Family, color = MaterialTheme.colors.onSurface)
                            }
                        }
                    }
                } else {
                    Text("No Font 2 Selected", modifier = Modifier.padding(top=4.dp))
                }
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
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = font1Family),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = newTo,
                onValueChange = { newTo = it },
                label = { Text("To") },
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = font2Family),
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
        } else {
            // Transliterate Text Tab
            var testInput by remember { mutableStateOf("") }
            var testOutput by remember { mutableStateOf("") }
            
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Text("Input Text (e.g. English)", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = testInput,
                        onValueChange = { 
                            testInput = it
                            val transliterator = intellibitz.sted.fontmap.DefaultTransliterator()
                            transliterator.setEntries(fontMap.entries)
                            testOutput = transliterator.parseLine(it) ?: ""
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = font1Family),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Text("Transliterated Output", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = testOutput,
                        onValueChange = {},
                        readOnly = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = font2Family),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

