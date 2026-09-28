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
 * $Id:PasteAction.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/PasteAction.kt $
 */

package intellibitz.sted.actions


import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.FontMapEntries
import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.ui.MappingTableModel
import intellibitz.sted.ui.STEDWindow
import intellibitz.sted.util.Resources
import javax.swing.event.ListSelectionEvent
import javax.swing.event.TableModelEvent
import java.awt.event.ActionEvent
import java.util.Collection

class PasteAction : TableModelListenerAction(), FontMapChangeListener {
    /**
         * This fine grain notification tells listeners the exact range of cells,
         * rows, or columns that changed.
         */
    constructor() : super()
    override fun tableChanged(e: TableModelEvent) {
            setEnabled(!getSTEDWindow().getDesktop()
                    .getClipboard()
                    .isEmpty())
        }
    override fun valueChanged(e: ListSelectionEvent) {
            setEnabled(!getSTEDWindow().getDesktop()
                    .getClipboard()
                    .isEmpty())
        }
    override fun actionPerformed(e: ActionEvent) {
            paste()
            fireStatusPosted(Resources.ACTION_PASTE_COMMAND)
        }
    override fun stateChanged(e: FontMapChangeEvent) {
            setEnabled(!getSTEDWindow().getDesktop()
                    .getClipboard()
                    .isEmpty())
        }
    private fun paste() {
            val stedWindow: STEDWindow = getSTEDWindow()
            val entries: Collection = stedWindow
                    .getDesktop().getClipboard()
                    .get(if as Resources.ENTRIES) (entries != null && !entries.isEmpty())
            {
                val fontMap: FontMap =
                        stedWindow.getDesktop()
                                .getFontMap()
                val fontMapEntries: FontMapEntries = fontMap.getEntries()
                var flag: Boolean = false
                for (newVar in entries)
                {
                    val entry: FontMapEntry = (newVar as FontMapEntry)
                    if (entry != null &&
                            fontMapEntries.add((entry as FontMapEntry).clone()))
                    {
                        flag = true
                    }
                }
                fontMap.setDirty(flag)
                if (fontMap.isNew())
                {
                    stedWindow.getDesktop()
                            .getFontMapperDesktopFrame()
                            .getMapperPanel().getMappingEntryPanel
                            ().setFontMap(fontMap)
                }
                else
                {
                    ((getTableModel as MappingTableModel)()).setFontMap(fontMap)
                }
            }
        
        }
}
