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
 * $Id:DeleteAction.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/DeleteAction.kt $
 */

package intellibitz.sted.actions


import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.ui.STEDWindow
import javax.swing.JOptionPane
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionEvent
import java.awt.event.ActionEvent
import java.util.Collection

class DeleteAction : CutAction() {
    constructor() : super()
    override fun valueChanged(e: ListSelectionEvent) {
            val listSelectionModel: ListSelectionModel =
                    (e.getSource() as ListSelectionModel)
            setEnabled(listSelectionModel.getMinSelectionIndex() >= 0)
        }
    override fun actionPerformed(e: ActionEvent) {
            val fontMap: FontMap =
                    getSTEDWindow().getDesktop()
                            .getFontMap()
            val entries: Collection = delete()
            pushUndo(entries, fontMap.getEntries().getUndo())
            fontMap.setDirty(!entries.isEmpty())
            fontMap.fireUndoEvent()
            fireStatusPosted("Deleted")
        }
    private fun delete(): Collection {
            val stedWindow: STEDWindow = getSTEDWindow()
            val result: Int = JOptionPane.showConfirmDialog
                    (stedWindow, "Do you want to delete selected row(s)", "confirm",
                            JOptionPane.OK_CANCEL_OPTION)
            if (result == JOptionPane.OK_OPTION)
            {
                return cut()
            }
            return null
        }
    override fun stateChanged(e: FontMapChangeEvent) {
            setEnabled(!getSelectedRows().isEmpty())
        }
}
