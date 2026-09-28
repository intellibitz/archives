package intellibitz.sted.ui

import intellibitz.sted.event.IMessageEventSource
import intellibitz.sted.event.IMessageListener
import intellibitz.sted.event.MessageEvent
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.FontMapEntries
import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.util.Resources
import java.util.ArrayList
import java.util.Collections
import javax.swing.table.AbstractTableModel

class MappingTableModel : AbstractTableModel(), IMessageEventSource {
    var fontMap: FontMap? = null
        private set
    private var fontMapEntries: FontMapEntries? = null
    private var entries: MutableList<FontMapEntry>? = null
    private var messageListener: IMessageListener? = null
    private val messageEvent = MessageEvent(this)

    fun setFontMap(fontMap: FontMap?) {
        this.fontMap = fontMap
        if (fontMap != null) {
            fontMapEntries = fontMap.entries
            entries = ArrayList(fontMapEntries!!.values())
            entries!!.sortWith(Collections.reverseOrder())
        }
        super.fireTableDataChanged()
    }

    override fun getRowCount(): Int {
        return fontMapEntries?.size() ?: 0
    }

    override fun getValueAt(row: Int, col: Int): Any? {
        if (col == 1) {
            return Resources.EQUALS
        }
        val entryList = entries
        if (entryList != null && entryList.isNotEmpty()) {
            val entry = entryList[row]
            return when (col) {
                0 -> entry.from
                2 -> entry.to
                3 -> entry.beginsWith
                4 -> entry.endsWith
                5 -> entry.followedBy ?: Resources.EMPTY_STRING
                6 -> entry.precededBy ?: Resources.EMPTY_STRING
                else -> Resources.EQUALS
            }
        }
        return Resources.EQUALS
    }

    fun getValueAt(row: Int): FontMapEntry? {
        return if (entries != null && entries!!.isNotEmpty()) entries!![row] else null
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        if (aValue != null) {
            val entry = entries!![rowIndex]
            when (columnIndex) {
                0 -> if (entry.from != aValue) {
                    val edited = entry.clone() as FontMapEntry
                    edited.from = aValue.toString()
                    if (!fontMapEntries!!.isValid(edited)) {
                        fireMessagePosted("Already mapped - Invalid Edit")
                        return
                    }
                    edited.from = entry.from
                    edited.status = Resources.ENTRY_STATUS_EDIT
                    fontMapEntries!!.undo.add(edited)
                    entry.from = aValue.toString()
                    entry.status = Resources.ENTRY_STATUS_EDIT
                    fontMapEntries!!.reKey(edited, entry)
                    fontMap!!.isDirty = true
                    super.fireTableCellUpdated(rowIndex, columnIndex)
                }
                2 -> if (entry.to != aValue) {
                    val edited = entry.clone() as FontMapEntry
                    edited.to = aValue.toString()
                    if (!fontMapEntries!!.isValidEdit(edited)) {
                        fireMessagePosted("Already mapped - Invalid Edit")
                        return
                    }
                    edited.to = entry.to
                    edited.status = Resources.ENTRY_STATUS_EDIT
                    fontMapEntries!!.undo.add(edited)
                    entry.to = aValue.toString()
                    entry.status = Resources.ENTRY_STATUS_EDIT
                    fontMapEntries!!.reKey(edited, entry)
                    fontMap!!.isDirty = true
                    super.fireTableCellUpdated(rowIndex, columnIndex)
                }
                3 -> {
                    val begins = aValue as Boolean
                    if (entry.beginsWith != begins) {
                        val edited = entry.clone() as FontMapEntry
                        edited.beginsWith = begins
                        if (!fontMapEntries!!.isValidEdit(edited)) {
                            fireMessagePosted("Already mapped - Invalid Edit")
                            return
                        }
                        edited.beginsWith = entry.beginsWith
                        edited.status = Resources.ENTRY_STATUS_EDIT
                        fontMapEntries!!.undo.add(edited)
                        entry.beginsWith = begins
                        entry.status = Resources.ENTRY_STATUS_EDIT
                        fontMapEntries!!.reKey(edited, entry)
                        fontMap!!.isDirty = true
                        super.fireTableCellUpdated(rowIndex, columnIndex)
                    }
                }
                4 -> {
                    val ends = aValue as Boolean
                    if (entry.endsWith != ends) {
                        val edited = entry.clone() as FontMapEntry
                        edited.endsWith = ends
                        if (!fontMapEntries!!.isValidEdit(edited)) {
                            fireMessagePosted("Already mapped - Invalid Edit")
                            return
                        }
                        edited.endsWith = entry.endsWith
                        edited.status = Resources.ENTRY_STATUS_EDIT
                        fontMapEntries!!.undo.add(edited)
                        entry.endsWith = ends
                        entry.status = Resources.ENTRY_STATUS_EDIT
                        fontMapEntries!!.reKey(edited, entry)
                        fontMap!!.isDirty = true
                        super.fireTableCellUpdated(rowIndex, columnIndex)
                    }
                }
                5 -> if (aValue != entry.followedBy) {
                    if (entry.followedBy == null && Resources.EMPTY_STRING == aValue) {
                        return
                    }
                    val edited = entry.clone() as FontMapEntry
                    edited.followedBy = aValue.toString()
                    if (!fontMapEntries!!.isValidEdit(edited)) {
                        fireMessagePosted("Already mapped - Invalid Edit")
                        return
                    }
                    edited.followedBy = entry.followedBy
                    edited.status = Resources.ENTRY_STATUS_EDIT
                    fontMapEntries!!.undo.add(edited)
                    entry.followedBy = aValue.toString()
                    entry.status = Resources.ENTRY_STATUS_EDIT
                    fontMapEntries!!.reKey(edited, entry)
                    fontMap!!.isDirty = true
                    super.fireTableCellUpdated(rowIndex, columnIndex)
                }
                6 -> if (aValue != entry.precededBy) {
                    if (entry.precededBy == null && Resources.EMPTY_STRING == aValue) {
                        return
                    }
                    val edited = entry.clone() as FontMapEntry
                    edited.precededBy = aValue.toString()
                    if (!fontMapEntries!!.isValidEdit(edited)) {
                        fireMessagePosted("Already mapped - Invalid Edit")
                        return
                    }
                    edited.precededBy = entry.precededBy
                    edited.status = Resources.ENTRY_STATUS_EDIT
                    fontMapEntries!!.undo.add(edited)
                    entry.precededBy = aValue.toString()
                    entry.status = Resources.ENTRY_STATUS_EDIT
                    fontMapEntries!!.reKey(edited, entry)
                    fontMap!!.isDirty = true
                    super.fireTableCellUpdated(rowIndex, columnIndex)
                }
            }
        }
    }

    override fun getColumnClass(c: Int): Class<*> {
        return getValueAt(0, c)?.javaClass ?: Any::class.java
    }

    override fun isCellEditable(row: Int, col: Int): Boolean {
        return col != 1
    }

    override fun getColumnCount(): Int {
        return names.size
    }

    override fun getColumnName(column: Int): String {
        return names[column]
    }

    private fun fireMessagePosted(message: String) {
        messageEvent.message = message
        messageListener?.messagePosted(messageEvent)
    }

    override fun fireMessagePosted() {
        messageListener?.messagePosted(messageEvent)
    }

    override fun addMessageListener(messageListener: IMessageListener) {
        this.messageListener = messageListener
    }

    companion object {
        private val names = arrayOf(
            Resources.getResource(Resources.TITLE_TABLE_COLUMN_SYMBOL1),
            Resources.getResource(Resources.TITLE_TABLE_COLUMN_EQUALS),
            Resources.getResource(Resources.TITLE_TABLE_COLUMN_SYMBOL2),
            Resources.getResource(Resources.TITLE_TABLE_COLUMN_FIRST_LETTER),
            Resources.getResource(Resources.TITLE_TABLE_COLUMN_LAST_LETTER),
            Resources.getResource(Resources.TITLE_TABLE_COLUMN_FOLLOWED_BY),
            Resources.getResource(Resources.TITLE_TABLE_COLUMN_PRECEDED_BY)
        )
    }
}
