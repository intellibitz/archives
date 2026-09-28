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
 * $Id:CutAction.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/CutAction.kt $
 */

package intellibitz.sted.actions


import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.ui.DesktopModel
import intellibitz.sted.ui.STEDWindow
import intellibitz.sted.util.Resources
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionEvent
import javax.swing.event.TableModelEvent
import java.awt.event.ActionEvent
import java.util.Collection
import java.util.Stack

class CutAction : TableModelListenerAction(), FontMapChangeListener {
    /**
         * This fine grain notification tells listeners the exact range of cells,
         * rows, or columns that changed.
         */
    constructor() : super()
    override fun tableChanged(e: TableModelEvent) {
        
        }
    override fun valueChanged(e: ListSelectionEvent) {
            setEnabled(((e.getSource() as ListSelectionModel))
                    .getMinSelectionIndex() >= 0)
        }
    override fun actionPerformed(e: ActionEvent) {
            val entries: Collection = cut()
            val stedWindow: STEDWindow = getSTEDWindow()
            stedWindow.getDesktop()
                    .addToClipboard(Resources.ENTRIES, entries)
            val fontMap: FontMap = stedWindow.getDesktop()
                    .getFontMap()
            pushUndo(entries, fontMap.getEntries().getUndo())
            fontMap.setDirty(!entries.isEmpty())
            fontMap.fireUndoEvent()
            fireStatusPosted("Cut")
        }
    fun pushUndo(entries: Collection, undo: java.util.Stack<FontMapEntry>) {
            for (entry in entries)
            {
                val fontMapEntry: FontMapEntry = (entry as FontMapEntry)
                fontMapEntry.setStatus(undo as Resources.ENTRY_STATUS_DELETE).push(fontMapEntry)
            }
        
        }
    fun cut(): Collection {
            val stedWindow: STEDWindow = getSTEDWindow()
            var desktopModel: DesktopModel =
                    stedWindow.getDesktop()
                            .getDesktopModel()
            val fontMap: FontMap = desktopModel.getFontMap()
            val entries: Collection =
                    fontMap.getEntries().remove(getSelectedRows())
    //        stedWindow.addListenersToDesktopFrame(fontMap)
    //        desktopModel.fireFontMapChangedEvent()
            return entries
        }
    override fun stateChanged(e: FontMapChangeEvent) {
            setEnabled(!getSelectedRows().isEmpty())
        }
}
