/**
 * Copyright (C) IntelliBitz Technologies.,  Muthu Ramadoss
 * 168, Medavakkam Main Road, Madipakkam, Chennai 600091, Tamilnadu, India.
 * http://www.intellibitz.com
 * training@intellibitz.com
 * +91 44 2247 5106
 * http://groups.google.com/group/etoe
 * http://sted.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * STED, Copyright (C) 2007 IntelliBitz Technologies
 * STED comes with ABSOLUTELY NO WARRANTY;
 * This is free software, and you are welcome
 * to redistribute it under the GNU GPL conditions;
 *
 * Visit http://www.gnu.org/ for GPL License terms.
 */

/**
 * $Id:MappingTableModel.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/MappingTableModel.kt $
 */

package intellibitz.sted.ui

import intellibitz.sted.event.IMessageEventSource
import intellibitz.sted.event.IMessageListener
import intellibitz.sted.event.MessageEvent
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.FontMapEntries
import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.util.Resources
import javax.swing.table.AbstractTableModel
import java.util.ArrayList
import java.util.Collections
import java.util.List

class MappingTableModel : AbstractTableModel(), IMessageEventSource {
    private var fontMap: FontMap? = null
    private var fontMapEntries: FontMapEntries? = null
    private var entries: List<FontMapEntry>? = null
    private var messageListener: IMessageListener? = null
    private var messageEvent: MessageEvent? = null
    constructor() : super() {
        messageEvent = MessageEvent(this)
    }
    fun setFontMap(fontMap: FontMap) {
        this = fontMap
        fontMapEntries = fontMap.getEntries()
        entries = ArrayList<FontMapEntry>(fontMapEntries.values())
        Collections.sort(entries, Collections.reverseOrder())
        super.fireTableDataChanged()
    }
    override fun getRowCount(): Int {
        if ((fontMapEntries == null)) {
            return 0
        }
        return fontMapEntries.size()
    }
    override fun getValueAt(row: Int, col: Int): Object {
        if ((col == 1)) {
            return Resources.EQUALS
        }
        if (((entries != null) && !entries.isEmpty())) {
            val entry: FontMapEntry = (entries.get(row) as FontMapEntry)
            if ((col == 0)) {
                return entry.getFrom()
            }
            else {
                if ((col == 2)) {
                    return entry.getTo()
                }
                else {
                    if ((col == 3)) {
                        if (entry.isBeginsWith()) {
                            return Boolean.TRUE
                        }
                        return Boolean.FALSE
                    }
                    else {
                        if ((col == 4)) {
                            if (entry.isEndsWith()) {
                                return Boolean.TRUE
                            }
                            return Boolean.FALSE
                        }
                        else {
                            if ((col == 5)) {
                                val val: String = entry.getFollowedBy()
                                return if ((val == null)) Resources.EMPTY_STRING else val
                            }
                            else {
                                if ((col == 6)) {
                                    val val: String = entry.getPrecededBy()
                                    return if ((val == null)) Resources.EMPTY_STRING else val
                                }
                            }
                        }
                    }
                }
            }
            return Resources.EQUALS
        }
        return Resources.EQUALS
    }
    override fun getValueAt(row: Int): FontMapEntry {
        if (((entries != null) && !entries.isEmpty())) {
            return (entries.get(row) as FontMapEntry)
        }
        return null
    }
    override fun setValueAt(aValue: Object, rowIndex: Int, columnIndex: Int) {
        if ((aValue != null)) {
            val entry: FontMapEntry = (entries.get(rowIndex) as FontMapEntry)
            if ((entry != null)) {
                when (columnIndex) {
                    0 -> {
                        if (!entry.getFrom()) {
                            val edited: FontMapEntry = (entry.clone() as FontMapEntry)
                            edited.setFrom(aValue.toString())
                            if (!fontMapEntries.isValid(edited)) {
                                fireMessagePosted("Already mapped - Invalid Edit")
                                break
                            }
                            edited.setFrom(entry.getFrom())
                            edited.setStatus(Resources.ENTRY_STATUS_EDIT)
                            fontMapEntries.getUndo()
                            entry.setFrom(aValue.toString())
                            entry.setStatus(Resources.ENTRY_STATUS_EDIT)
                            fontMapEntries.reKey(edited, entry)
                            fontMap.setDirty(true)
                            super.fireTableCellUpdated(rowIndex, columnIndex)
                        }
                        break
                    }
                    2 -> {
                        if (!entry.getTo()) {
                            val edited: FontMapEntry = (entry.clone() as FontMapEntry)
                            edited.setTo(aValue.toString())
                            if (!fontMapEntries.isValidEdit(edited)) {
                                fireMessagePosted("Already mapped - Invalid Edit")
                                break
                            }
                            edited.setTo(entry.getTo())
                            edited.setStatus(Resources.ENTRY_STATUS_EDIT)
                            fontMapEntries.getUndo()
                            entry.setTo(aValue.toString())
                            entry.setStatus(Resources.ENTRY_STATUS_EDIT)
                            fontMapEntries.reKey(edited, entry)
                            fontMap.setDirty(true)
                            super.fireTableCellUpdated(rowIndex, columnIndex)
                        }
                        break
                    }
                    3 -> {
                        val begins: Boolean = (aValue as Boolean)
                        if ((!entry.isBeginsWith() == begins)) {
                            val edited: FontMapEntry = (entry.clone() as FontMapEntry)
                            edited.setBeginsWith(begins)
                            if (!fontMapEntries.isValidEdit(edited)) {
                                fireMessagePosted("Already mapped - Invalid Edit")
                                break
                            }
                            edited.setBeginsWith(entry.isBeginsWith())
                            edited.setStatus(Resources.ENTRY_STATUS_EDIT)
                            fontMapEntries.getUndo()
                            entry.setBeginsWith(begins)
                            entry.setStatus(Resources.ENTRY_STATUS_EDIT)
                            fontMapEntries.reKey(edited, entry)
                            fontMap.setDirty(true)
                            super.fireTableCellUpdated(rowIndex, columnIndex)
                        }
                        break
                    }
                    4 -> {
                        val ends: Boolean = (aValue as Boolean)
                        if ((!entry.isEndsWith() == ends)) {
                            val edited: FontMapEntry = (entry.clone() as FontMapEntry)
                            edited.setEndsWith(ends)
                            if (!fontMapEntries.isValidEdit(edited)) {
                                fireMessagePosted("Already mapped - Invalid Edit")
                                break
                            }
                            edited.setEndsWith(entry.isEndsWith())
                            edited.setStatus(Resources.ENTRY_STATUS_EDIT)
                            fontMapEntries.getUndo()
                            entry.setEndsWith(ends)
                            entry.setStatus(Resources.ENTRY_STATUS_EDIT)
                            fontMapEntries.reKey(edited, entry)
                            fontMap.setDirty(true)
                            super.fireTableCellUpdated(rowIndex, columnIndex)
                        }
                        break
                    }
                    5 -> {
                        if (!aValue.equals(entry.getFollowedBy())) {
                            if (((entry.getFollowedBy() == null) && Resources.EMPTY_STRING.equals(aValue))) {
                                break
                            }
                            val edited: FontMapEntry = (entry.clone() as FontMapEntry)
                            edited.setFollowedBy(aValue.toString())
                            if (!fontMapEntries.isValidEdit(edited)) {
                                fireMessagePosted("Already mapped - Invalid Edit")
                                break
                            }
                            edited.setFollowedBy(entry.getFollowedBy())
                            edited.setStatus(Resources.ENTRY_STATUS_EDIT)
                            fontMapEntries.getUndo()
                            entry.setFollowedBy(aValue.toString())
                            entry.setStatus(Resources.ENTRY_STATUS_EDIT)
                            fontMapEntries.reKey(edited, entry)
                            fontMap.setDirty(true)
                            super.fireTableCellUpdated(rowIndex, columnIndex)
                        }
                        break
                    }
                    6 -> {
                        if (!aValue.equals(entry.getPrecededBy())) {
                            if (((entry.getPrecededBy() == null) && Resources.EMPTY_STRING.equals(aValue))) {
                                break
                            }
                            val edited: FontMapEntry = (entry.clone() as FontMapEntry)
                            edited.setPrecededBy(aValue.toString())
                            if (!fontMapEntries.isValidEdit(edited)) {
                                fireMessagePosted("Already mapped - Invalid Edit")
                                break
                            }
                            edited.setPrecededBy(entry.getPrecededBy())
                            edited.setStatus(Resources.ENTRY_STATUS_EDIT)
                            fontMapEntries.getUndo()
                            entry.setPrecededBy(aValue.toString())
                            entry.setStatus(Resources.ENTRY_STATUS_EDIT)
                            fontMapEntries.reKey(edited, entry)
                            fontMap.setDirty(true)
                            super.fireTableCellUpdated(rowIndex, columnIndex)
                        }
                        break
                    }
                }
            }
        }
    }
    override fun getColumnClass(c: Int): Class {
        return getValueAt(0, c)
    }
    override fun isCellEditable(row: Int, col: Int): Boolean {
        return (col == 1)
    }
    override fun getColumnCount(): Int {
        return names.length
    }
    override fun getColumnName(column: Int): String {
        return names[column]
    }
    fun getFontMap(): FontMap {
        return fontMap
    }
    private fun fireMessagePosted(message: String) {
        messageEvent.setMessage(message)
        messageListener.messagePosted(messageEvent)
    }
    fun fireMessagePosted() {
        messageListener.messagePosted(messageEvent)
    }
    fun addMessageListener(messageListener: IMessageListener) {
        this = messageListener
    }

    companion object {
        private val names: Array<String> = arrayOf(Resources.getResource(Resources.TITLE_TABLE_COLUMN_SYMBOL1), Resources.getResource(Resources.TITLE_TABLE_COLUMN_EQUALS), Resources.getResource(Resources.TITLE_TABLE_COLUMN_SYMBOL2), Resources.getResource(Resources.TITLE_TABLE_COLUMN_FIRST_LETTER), Resources.getResource(Resources.TITLE_TABLE_COLUMN_LAST_LETTER), Resources.getResource(Resources.TITLE_TABLE_COLUMN_FOLLOWED_BY), Resources.getResource(Resources.TITLE_TABLE_COLUMN_PRECEDED_BY))
    }
}
