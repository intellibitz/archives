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

    fun openFontMap(file: File) {
        val fontMap = FontMap(file)
        intellibitz.sted.io.FontMapReader.read(fontMap)
        openTabs = openTabs + fontMap
        selectedTabIndex = openTabs.lastIndex
    }

    fun closeTab(index: Int) {
        val newList = openTabs.toMutableList()
        newList.removeAt(index)
        openTabs = newList
        if (selectedTabIndex >= openTabs.size) {
            selectedTabIndex = openTabs.lastIndex
        }
    }
    
    val currentFontMap: FontMap?
        get() = openTabs.getOrNull(selectedTabIndex)
}
