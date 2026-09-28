package intellibitz.sted.actions

import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.util.Resources
import java.awt.event.ActionEvent
import java.util.Stack
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionEvent
import javax.swing.event.TableModelEvent

open class CutAction : TableModelListenerAction(), FontMapChangeListener {
    override fun tableChanged(e: TableModelEvent) {}

    override fun valueChanged(e: ListSelectionEvent) {
        isEnabled = (e.source as ListSelectionModel).minSelectionIndex >= 0
    }

    override fun actionPerformed(e: ActionEvent) {
        val entries = cut()
        val stedWindow = getSTEDWindow()
        stedWindow.desktop!!.addToClipboard(Resources.ENTRIES, entries)
        val fontMap = stedWindow.desktop!!.fontMap!!
        pushUndo(entries, fontMap.entries.undo)
        fontMap.isDirty = entries.isNotEmpty()
        fontMap.fireUndoEvent()
        fireStatusPosted("Cut")
    }

    fun pushUndo(entries: Collection<*>, undo: Stack<FontMapEntry>) {
        for (entry in entries) {
            val fontMapEntry = entry as FontMapEntry
            fontMapEntry.status = Resources.ENTRY_STATUS_DELETE
            undo.push(fontMapEntry)
        }
    }

    fun cut(): Collection<*> {
        val stedWindow = getSTEDWindow()
        val desktopModel = stedWindow.desktop!!.desktopModel!!
        val fontMap = desktopModel.fontMap!!
        return fontMap.entries.remove(getSelectedRows())
    }

    override fun stateChanged(e: FontMapChangeEvent) {
        isEnabled = getSelectedRows().isNotEmpty()
    }
}
