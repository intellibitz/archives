/**
 * Copyright (C) IntelliBitz Technologies.,  Muthu Ramadoss
 * 168, Medavakkam Main Road, Madipakkam, Chennai 600091, Tamilnadu, India.
 * http://www.intellibitz.com
 * training@intellibitz.com
 * +91 44 2247 5106
 * http://groups.google.com/group/etoe
 * http://sted.sourceforge.net
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * <p>
 * STED, Copyright (C) 2007 IntelliBitz Technologies
 * STED comes with ABSOLUTELY NO WARRANTY;
 * This is free software, and you are welcome
 * to redistribute it under the GNU GPL conditions;
 * <p>
 * Visit http://www.gnu.org/ for GPL License terms.
 * <p>
 * $Id:CutAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/CutAction.java $
 */

/**
 * $Id:CutAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/CutAction.java $
 */

package intellibitz.sted.actions;

import intellibitz.sted.event.FontMapChangeEvent;
import intellibitz.sted.event.FontMapChangeListener;
import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.fontmap.FontMapEntry;
import intellibitz.sted.ui.DesktopModel;
import intellibitz.sted.ui.STEDWindow;
import intellibitz.sted.util.Resources;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Stack;

public class CutAction
        extends TableModelListenerAction
        implements FontMapChangeListener {
    public CutAction() {
        super();
    }


    /**
     * This fine grain notification tells listeners the exact range of cells,
     * rows, or columns that changed.
     */
    public void tableChanged(TableModelEvent e) {
    }

    public void valueChanged(ListSelectionEvent e) {
        setEnabled(((ListSelectionModel) e.getSource())
                .getMinSelectionIndex() >= 0);
    }

    public void actionPerformed(ActionEvent e) {
        final Collection entries = cut();
        final STEDWindow stedWindow = getSTEDWindow();
        stedWindow.getDesktop()
                .addToClipboard(Resources.ENTRIES, entries);
        final FontMap fontMap = stedWindow.getDesktop()
                .getFontMap();
        pushUndo(entries, fontMap.getEntries().getUndo());
        fontMap.setDirty(!entries.isEmpty());
        fontMap.fireUndoEvent();
        fireStatusPosted("Cut");
    }

    public void pushUndo(Collection entries, Stack<FontMapEntry> undo) {
        for (Object entry : entries) {
            final FontMapEntry fontMapEntry = (FontMapEntry) entry;
            fontMapEntry.setStatus(Resources.ENTRY_STATUS_DELETE);
            undo.push(fontMapEntry);
        }
    }

    Collection cut() {
        final STEDWindow stedWindow = getSTEDWindow();
        DesktopModel desktopModel =
                stedWindow.getDesktop()
                        .getDesktopModel();
        final FontMap fontMap = desktopModel.getFontMap();
        final Collection entries =
                fontMap.getEntries().remove(getSelectedRows());
//        stedWindow.addListenersToDesktopFrame(fontMap);
//        desktopModel.fireFontMapChangedEvent();
        return entries;
    }

    public void stateChanged(FontMapChangeEvent e) {
        setEnabled(!getSelectedRows().isEmpty());
    }

}
