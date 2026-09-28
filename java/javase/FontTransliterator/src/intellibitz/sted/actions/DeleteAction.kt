package intellibitz.sted.actions

import intellibitz.sted.event.FontMapChangeEvent
import java.awt.event.ActionEvent
import javax.swing.JOptionPane
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionEvent

class DeleteAction : CutAction() {
    override fun valueChanged(e: ListSelectionEvent) {
        val listSelectionModel = e.source as ListSelectionModel
        isEnabled = listSelectionModel.minSelectionIndex >= 0
    }

    override fun actionPerformed(e: ActionEvent) {
        val fontMap = getSTEDWindow().desktop!!.fontMap!!
        val entries = delete()
        if (entries != null) {
            pushUndo(entries, fontMap.entries.undo)
            fontMap.isDirty = entries.isNotEmpty()
            fontMap.fireUndoEvent()
            fireStatusPosted("Deleted")
        }
    }

    private fun delete(): Collection<*>? {
        val stedWindow = getSTEDWindow()
        val result = JOptionPane.showConfirmDialog(
            stedWindow, "Do you want to delete selected row(s)", "confirm",
            JOptionPane.OK_CANCEL_OPTION
        )
        if (result == JOptionPane.OK_OPTION) {
            return cut()
        }
        return null
    }

    override fun stateChanged(e: FontMapChangeEvent) {
        isEnabled = getSelectedRows().isNotEmpty()
    }
}
