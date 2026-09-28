package intellibitz.sted.actions

import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.ui.DesktopFrame
import intellibitz.sted.ui.STEDWindow
import intellibitz.sted.ui.TabDesktop
import intellibitz.sted.util.Resources
import java.awt.event.ActionEvent
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.TableModelEvent

class UndoAction : TableModelListenerAction(), FontMapChangeListener, ChangeListener {

    override fun actionPerformed(e: ActionEvent) {
        undo(getSTEDWindow())
        fireStatusPosted("Undo")
        val fontMap = getSTEDWindow().desktop!!.fontMap!!
        fontMap.fireUndoEvent()
        fontMap.fireRedoEvent()
    }

    fun undo(stedWindow: STEDWindow) {
        val fontMap = stedWindow.desktop!!.fontMap!!
        val fontMapEntries = fontMap.entries
        val undoEntries = fontMapEntries.undo
        if (undoEntries.isEmpty()) {
            return
        }
        val fontMapEntry = undoEntries.pop()
        if (fontMapEntry.isAdded()) {
            val current = fontMapEntries.remove(fontMapEntry.id)!!
            current.status = Resources.ENTRY_STATUS_DELETE
            fontMapEntries.redo.push(current)
        } else if (fontMapEntry.isEdited()) {
            val current = fontMapEntries.remove(fontMapEntry.id)!!
            fontMapEntries.redo.push(current)
            fontMapEntries.add(fontMapEntry)
        } else if (fontMapEntry.isDeleted()) {
            fontMapEntry.status = Resources.ENTRY_STATUS_ADD
            fontMapEntries.add(fontMapEntry)
            fontMapEntries.redo.push(fontMapEntry)
        }
        stedWindow.desktop!!.desktopModel!!.fireFontMapChangedEvent()
    }

    private fun setEnabled(fontMap: FontMap): Boolean {
        val empty = fontMap.entries.undo.isEmpty()
        isEnabled = !empty
        return !empty
    }

    override fun stateChanged(e: FontMapChangeEvent) {
        val fontMap = e.fontMap!!
        if (!setEnabled(fontMap)) {
            fontMap.isDirty = false
        }
    }

    override fun tableChanged(e: TableModelEvent) {
        setEnabled(getSTEDWindow().desktop!!.fontMap!!)
    }

    override fun stateChanged(e: ChangeEvent) {
        val desktop = e.source as TabDesktop
        val index = desktop.selectedIndex
        if (index > -1) {
            val dframe = desktop.getComponentAt(index) as DesktopFrame
            setEnabled(dframe.desktopModel!!.fontMap!!)
        }
    }
}
