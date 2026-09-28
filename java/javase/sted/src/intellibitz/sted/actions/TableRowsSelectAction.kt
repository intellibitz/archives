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
 * $Id:TableRowsSelectAction.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/TableRowsSelectAction.kt $
 */

package intellibitz.sted.actions


import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.ui.MappingTableModel
import javax.swing.JTable
import javax.swing.event.ListSelectionListener
import javax.swing.table.TableModel
import java.util.ArrayList
import java.util.Collection

abstract class TableRowsSelectAction : STEDWindowAction(), ListSelectionListener {
    private var table: JTable? = null
    protected constructor() : super()
    fun getTableModel(): TableModel {
            return table.getModel()
        }
    fun setTable(table: JTable) {
            this.table = table
        }
    fun selectAll() {
            table.selectAll()
        }
    fun getSelectedRows(): Collection<FontMapEntry> {
            final int row[] = table.getSelectedRows()
            val rows: Collection<FontMapEntry> =
                    new ArrayList<FontMapEntry>(row.length)
            for (newVar in row)
            {
                rows.add(((table.getModel() as MappingTableModel)).getValueAt(newVar))
            }
            return rows
        }
    fun copySelectedRows(): Collection {
            final int row[] = table.getSelectedRows()
            val rows: Collection<FontMapEntry> =
                    new ArrayList<FontMapEntry>(row.length)
            for (newVar in row)
            {
                rows.add((FontMapEntry) ((table.getModel() as MappingTableModel))
                        .getValueAt(newVar).clone())
            }
            return rows
        }
}
