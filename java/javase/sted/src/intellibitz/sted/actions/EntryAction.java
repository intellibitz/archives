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
 * $Id:EntryAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/EntryAction.java $
 */

package intellibitz.sted.actions;

import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.fontmap.FontMapEntries;
import intellibitz.sted.fontmap.FontMapEntry;
import intellibitz.sted.ui.MappingEntryPanel;
import intellibitz.sted.ui.STEDWindow;
import intellibitz.sted.ui.TabDesktop;
import intellibitz.sted.util.Resources;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class EntryAction
        extends STEDWindowAction
{
    private MappingEntryPanel mappingEntryPanel;

    public EntryAction()
    {
        super();
    }

    public void setFontEntryPanel(MappingEntryPanel mappingEntryPanel)
    {
        this.mappingEntryPanel = mappingEntryPanel;
    }

    public void keyReleased(KeyEvent e)
    {
        if (KeyEvent.VK_ENTER == e.getKeyCode())
        {
            addFontMapEntry();
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        addFontMapEntry();
    }

    private void addFontMapEntry()
    {
        final String key1 = mappingEntryPanel.getWord1().getText();
        final String key2 = mappingEntryPanel.getWord2().getText();
        final STEDWindow stedWindow = getSTEDWindow();
        if (key1 != null && key2 != null && key1.length() > 0 &&
                key2.length() > 0)
        {
            final TabDesktop tabDesktop =
                    stedWindow.getDesktop();
            final FontMap fontMap = tabDesktop.getFontMap();
            final FontMapEntries entries = fontMap.getEntries();
            final FontMapEntry fontMapEntry = new FontMapEntry(key1, key2);
            if (entries.add(fontMapEntry))
            {
                // add it to the undo list too
                fontMapEntry.setStatus(Resources.ENTRY_STATUS_ADD);
                entries.getUndo().push(fontMapEntry);
                fontMap.setDirty(true);
                fontMap.fireUndoEvent();
                tabDesktop.getDesktopModel()
                        .fireFontMapChangedEvent();
                fireStatusPosted(
                        "New FontMap Entry Added");
            }
            else
            {
                fireMessagePosted("Already mapped.. Invalid Add");
            }
        }
        else
        {
            fireMessagePosted(
                    "Insufficient data.. Please use both keypads to map characters ");
        }
    }

}
