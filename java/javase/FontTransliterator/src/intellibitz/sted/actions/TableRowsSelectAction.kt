package intellibitz.sted.actions

import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.ui.MappingTableModel

import javax.swing.JTable
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.table.TableModel
import java.util.ArrayList

abstract class TableRowsSelectAction : STEDWindowAction(), ListSelectionListener {
    var table: JTable? = null

    fun getTableModel(): TableModel? {
        return table?.model
    }

    fun selectAll() {
        table?.selectAll()
    }

    fun getSelectedRows(): Collection<FontMapEntry> {
        val row = table?.selectedRows ?: IntArray(0)
        val rows = ArrayList<FontMapEntry>(row.size)
        for (newVar in row) {
            rows.add((table?.model as MappingTableModel).getValueAt(newVar)!!)
        }
        return rows
    }

    fun copySelectedRows(): Collection<FontMapEntry> {
        val row = table?.selectedRows ?: IntArray(0)
        val rows = ArrayList<FontMapEntry>(row.size)
        for (newVar in row) {
            rows.add((table?.model as MappingTableModel).getValueAt(newVar)!!.clone() as FontMapEntry)
        }
        return rows
    }
}
