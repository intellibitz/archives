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
 * $Id:RedoAction.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/RedoAction.kt $
 */

package intellibitz.sted.actions


import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.FontMapEntries
import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.ui.DesktopFrame
import intellibitz.sted.ui.STEDWindow
import intellibitz.sted.ui.TabDesktop
import intellibitz.sted.util.Resources
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.TableModelEvent
import java.awt.event.ActionEvent
import java.util.Stack

class RedoAction : TableModelListenerAction() {
    /**
         * This fine grain notification tells listeners the exact range of cells,
         * rows, or columns that changed.
         */
    /**
         * This listens for state change in TabDesktop, when tab selection is made
         *
         * @param e
         */
    constructor() : super()
    override fun actionPerformed(e: ActionEvent) {
    
            redo(getSTEDWindow())
            val fontMap: FontMap =
                    getSTEDWindow().getDesktop()
                            .getFontMap()
            fontMap.setDirty(true)
            fireStatusPosted("Redo")
            fontMap.fireUndoEvent()
            fontMap.fireRedoEvent()
        }
    fun redo(stedWindow: STEDWindow) {
            val fontMapEntries: FontMapEntries =
                    stedWindow.getDesktop()
                            .getFontMap().getEntries()
            val redoEntries: java.util.Stack<FontMapEntry> = fontMapEntries.getRedo()
            if (redoEntries.isEmpty())
            {
                return
            }
            val fontMapEntry: FontMapEntry = redoEntries.pop()
            if (fontMapEntry.isAdded())
            {
                val current: FontMapEntry =
                        fontMapEntries.remove(fontMapEntry.getId())
                // change the status when pushing to the redo stack
                current.setStatus(fontMapEntries.getUndo() as Resources.ENTRY_STATUS_DELETE).push(current)
            }
            else if (fontMapEntry.isEdited())
            {
                val current: FontMapEntry =
                        fontMapEntries.remove(fontMapEntry.getId())
                fontMapEntries.getUndo().push(current)
                fontMapEntries.add(fontMapEntry)
            }
            else if (fontMapEntry.isDeleted())
            {
                // change the status when pushing to the redo stack
                fontMapEntry.setStatus(fontMapEntries as Resources.ENTRY_STATUS_ADD).add(fontMapEntry)
                fontMapEntries.getUndo().push(fontMapEntry)
            }
            stedWindow.getDesktop()
                    .getDesktopModel().fireFontMapChangedEvent()
        }
    private fun setEnabled(fontMap: FontMap): Boolean {
            var empty: Boolean = fontMap.getEntries().getRedo().isEmpty()
            setEnabled(!empty)
            return !empty
        }
    override fun stateChanged(e: FontMapChangeEvent) {
            val fontMap: FontMap = e.getFontMap()
            if (!setEnabled(fontMap))
            {
                fontMap.setDirty(false)
            }
        
        }
    override fun tableChanged(e: TableModelEvent) {
            setEnabled(getSTEDWindow().getDesktop()
                    .getFontMap())
        }
    override fun stateChanged(e: ChangeEvent) {
            var desktop: TabDesktop = (e.getSource() as TabDesktop)
            var index: Int = desktop.getSelectedIndex()
            if (index > -1)
            {
                var dframe: DesktopFrame =
                        (desktop as DesktopFrame).getComponentAt(
                                index)
                setEnabled(dframe.getModel().getFontMap())
            }
    
        
        }
}
