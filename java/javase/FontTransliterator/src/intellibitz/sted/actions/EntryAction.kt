package intellibitz.sted.actions

import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.ui.MappingEntryPanel
import intellibitz.sted.util.Resources
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent

class EntryAction : STEDWindowAction() {
    private var mappingEntryPanel: MappingEntryPanel? = null

    fun setFontEntryPanel(mappingEntryPanel: MappingEntryPanel) {
        this.mappingEntryPanel = mappingEntryPanel
    }

    override fun keyReleased(e: KeyEvent) {
        if (KeyEvent.VK_ENTER == e.keyCode) {
            addFontMapEntry()
        }
    }

    override fun actionPerformed(e: ActionEvent) {
        addFontMapEntry()
    }

    private fun addFontMapEntry() {
        val key1 = mappingEntryPanel?.word1?.text
        val key2 = mappingEntryPanel?.word2?.text
        val stedWindow = getSTEDWindow()
        
        if (!key1.isNullOrEmpty() && !key2.isNullOrEmpty()) {
            val tabDesktop = stedWindow.desktop!!
            val fontMap = tabDesktop.fontMap!!
            val entries = fontMap.entries
            val fontMapEntry = FontMapEntry(key1, key2)
            
            if (entries.add(fontMapEntry)) {
                fontMapEntry.status = Resources.ENTRY_STATUS_ADD
                entries.undo.push(fontMapEntry)
                fontMap.isDirty = true
                fontMap.fireUndoEvent()
                tabDesktop.desktopModel!!.fireFontMapChangedEvent()
                fireStatusPosted("New FontMap Entry Added")
            } else {
                fireMessagePosted("Already mapped.. Invalid Add")
            }
        } else {
            fireMessagePosted("Insufficient data.. Please use both keypads to map characters ")
        }
    }
}
