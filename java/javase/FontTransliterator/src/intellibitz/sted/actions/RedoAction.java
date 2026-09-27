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
 * $Id:RedoAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/RedoAction.java $
 */

/**
 * $Id:RedoAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/RedoAction.java $
 */

package intellibitz.sted.actions;

import intellibitz.sted.event.FontMapChangeEvent;
import intellibitz.sted.event.FontMapChangeListener;
import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.fontmap.FontMapEntries;
import intellibitz.sted.fontmap.FontMapEntry;
import intellibitz.sted.ui.DesktopFrame;
import intellibitz.sted.ui.STEDWindow;
import intellibitz.sted.ui.TabDesktop;
import intellibitz.sted.util.Resources;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import java.awt.event.ActionEvent;
import java.util.Stack;

public class RedoAction
        extends TableModelListenerAction
        implements FontMapChangeListener,
        ChangeListener {
    public RedoAction() {
        super();
    }

    public void actionPerformed(ActionEvent e) {

        redo(getSTEDWindow());
        final FontMap fontMap =
                getSTEDWindow().getDesktop()
                        .getFontMap();
        fontMap.setDirty(true);
        fireStatusPosted("Redo");
        fontMap.fireUndoEvent();
        fontMap.fireRedoEvent();
    }

    public void redo(STEDWindow stedWindow) {
        final FontMapEntries fontMapEntries =
                stedWindow.getDesktop()
                        .getFontMap().getEntries();
        final Stack<FontMapEntry> redoEntries = fontMapEntries.getRedo();
        if (redoEntries.isEmpty()) {
            return;
        }
        final FontMapEntry fontMapEntry = redoEntries.pop();
        if (fontMapEntry.isAdded()) {
            final FontMapEntry current =
                    fontMapEntries.remove(fontMapEntry.getId());
            // change the status when pushing to the redo stack
            current.setStatus(Resources.ENTRY_STATUS_DELETE);
            fontMapEntries.getUndo().push(current);
        } else if (fontMapEntry.isEdited()) {
            final FontMapEntry current =
                    fontMapEntries.remove(fontMapEntry.getId());
            fontMapEntries.getUndo().push(current);
            fontMapEntries.add(fontMapEntry);
        } else if (fontMapEntry.isDeleted()) {
            // change the status when pushing to the redo stack
            fontMapEntry.setStatus(Resources.ENTRY_STATUS_ADD);
            fontMapEntries.add(fontMapEntry);
            fontMapEntries.getUndo().push(fontMapEntry);
        }
        stedWindow.getDesktop()
                .getDesktopModel().fireFontMapChangedEvent();
    }

    private boolean setEnabled(FontMap fontMap) {
        boolean empty = fontMap.getEntries().getRedo().isEmpty();
        setEnabled(!empty);
        return !empty;
    }

    public void stateChanged(FontMapChangeEvent e) {
        final FontMap fontMap = e.getFontMap();
        if (!setEnabled(fontMap)) {
            fontMap.setDirty(false);
        }
    }

    /**
     * This fine grain notification tells listeners the exact range of cells,
     * rows, or columns that changed.
     */
    public void tableChanged(TableModelEvent e) {
        setEnabled(getSTEDWindow().getDesktop()
                .getFontMap());
    }

    /**
     * This listens for state change in TabDesktop, when tab selection is made
     *
     * @param e
     */
    public void stateChanged(ChangeEvent e) {
        TabDesktop desktop = (TabDesktop) e.getSource();
        int index = desktop.getSelectedIndex();
        if (index > -1) {
            DesktopFrame dframe =
                    (DesktopFrame) desktop.getComponentAt(
                            index);
            setEnabled(dframe.getModel().getFontMap());
        }

    }
}
