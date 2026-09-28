package intellibitz.sted.ui.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import intellibitz.sted.fontmap.FontMap
import java.io.File

class AppState {
    var openTabs by mutableStateOf(listOf<FontMap>())
    var selectedTabIndex by mutableStateOf(0)

    fun newFontMap() {
        openTabs = openTabs + FontMap()
        selectedTabIndex = openTabs.lastIndex
    }

    fun openFontMap() {
        val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Open FontMap", java.awt.FileDialog.LOAD)
        dialog.file = "*.xml"
        dialog.isVisible = true
        val filename = dialog.file
        if (filename != null) {
            val file = File(dialog.directory, filename)
            val fontMap = FontMap(file)
            intellibitz.sted.io.FontMapReader.read(fontMap)
            openTabs = openTabs + fontMap
            selectedTabIndex = openTabs.lastIndex
        }
    }

    fun saveFontMap() {
        val current = currentFontMap ?: return
        var file = current.fontMapFile
        if (file == null || current.isNew()) {
            val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Save FontMap", java.awt.FileDialog.SAVE)
            dialog.file = "*.xml"
            dialog.isVisible = true
            val filename = dialog.file ?: return
            file = File(dialog.directory, filename)
            current.fontMapFile = file
        }
        intellibitz.sted.io.FontMapXMLWriter.write(current)
        current.isDirty = false
        // Trigger recomposition for tab title
        val tabs = openTabs.toMutableList()
        openTabs = listOf()
        openTabs = tabs
    }

    fun closeTab(index: Int) {
        val newList = openTabs.toMutableList()
        newList.removeAt(index)
        openTabs = newList
        if (selectedTabIndex >= openTabs.size) {
            selectedTabIndex = openTabs.lastIndex
        }
    }
    
    fun transliterateFile() {
        val current = currentFontMap ?: return
        
        val openDialog = java.awt.FileDialog(null as java.awt.Frame?, "Open File to Transliterate", java.awt.FileDialog.LOAD)
        openDialog.isVisible = true
        val inFileStr = openDialog.file ?: return
        val inFile = File(openDialog.directory, inFileStr)
        
        val saveDialog = java.awt.FileDialog(null as java.awt.Frame?, "Save Transliterated File", java.awt.FileDialog.SAVE)
        saveDialog.isVisible = true
        val outFileStr = saveDialog.file ?: return
        val outFile = File(saveDialog.directory, outFileStr)
        
        val converter = intellibitz.sted.fontmap.Converter(current, inFile, outFile)
        converter.run()
    }
    
    val currentFontMap: FontMap?
        get() = openTabs.getOrNull(selectedTabIndex)
}
