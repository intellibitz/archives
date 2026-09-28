package intellibitz.sted.actions

import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.util.Resources
import java.awt.event.ActionEvent
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionEvent
import javax.swing.event.TableModelEvent

class CopyAction : TableModelListenerAction(), FontMapChangeListener {
    override fun tableChanged(e: TableModelEvent) {}

    override fun valueChanged(e: ListSelectionEvent) {
        val listSelectionModel = e.source as ListSelectionModel
        isEnabled = listSelectionModel.minSelectionIndex >= 0
    }

    override fun actionPerformed(e: ActionEvent) {
        val stedWindow = getSTEDWindow()
        stedWindow.desktop!!.addToClipboard(Resources.ENTRIES, copySelectedRows())
    }

    override fun stateChanged(e: FontMapChangeEvent) {
        isEnabled = getSelectedRows().isNotEmpty()
    }
}
