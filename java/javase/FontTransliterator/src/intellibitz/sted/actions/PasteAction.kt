package intellibitz.sted.actions

import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.ui.MappingTableModel
import intellibitz.sted.util.Resources
import java.awt.event.ActionEvent
import javax.swing.event.ListSelectionEvent
import javax.swing.event.TableModelEvent

class PasteAction : TableModelListenerAction(), FontMapChangeListener {
    override fun tableChanged(e: TableModelEvent) {
        isEnabled = !getSTEDWindow().desktop!!.clipboard.isEmpty()
    }

    override fun valueChanged(e: ListSelectionEvent) {
        isEnabled = !getSTEDWindow().desktop!!.clipboard.isEmpty()
    }

    override fun actionPerformed(e: ActionEvent) {
        paste()
        fireStatusPosted(Resources.ACTION_PASTE_COMMAND)
    }

    override fun stateChanged(e: FontMapChangeEvent) {
        isEnabled = !getSTEDWindow().desktop!!.clipboard.isEmpty()
    }

    private fun paste() {
        val stedWindow = getSTEDWindow()
        val entries = stedWindow.desktop!!.clipboard[Resources.ENTRIES]
        if (entries != null && entries.isNotEmpty()) {
            val fontMap = stedWindow.desktop!!.fontMap!!
            val fontMapEntries = fontMap.entries
            var flag = false
            for (newVar in entries) {
                val entry = newVar as FontMapEntry
                if (fontMapEntries.add(entry.clone() as FontMapEntry)) {
                    flag = true
                }
            }
            fontMap.isDirty = flag
            if (fontMap.isNew()) {
                stedWindow.desktop!!.fontMapperDesktopFrame!!.mapperPanel!!.mappingEntryPanel!!.setFontMap(fontMap)
            } else {
                (getTableModel() as MappingTableModel).setFontMap(fontMap)
            }
        }
    }
}
