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
 * $Id:PasteAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/PasteAction.java $
 */

/**
 * $Id:PasteAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/PasteAction.java $
 */

package intellibitz.sted.actions;

import intellibitz.sted.event.FontMapChangeEvent;
import intellibitz.sted.event.FontMapChangeListener;
import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.fontmap.FontMapEntries;
import intellibitz.sted.fontmap.FontMapEntry;
import intellibitz.sted.ui.MappingTableModel;
import intellibitz.sted.ui.STEDWindow;
import intellibitz.sted.util.Resources;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import java.awt.event.ActionEvent;
import java.util.Collection;

public class PasteAction
        extends TableModelListenerAction
        implements FontMapChangeListener {
    public PasteAction() {
        super();
    }


    /**
     * This fine grain notification tells listeners the exact range of cells,
     * rows, or columns that changed.
     */
    public void tableChanged(TableModelEvent e) {
        setEnabled(!getSTEDWindow().getDesktop()
                .getClipboard()
                .isEmpty());
    }

    public void valueChanged(ListSelectionEvent e) {
        setEnabled(!getSTEDWindow().getDesktop()
                .getClipboard()
                .isEmpty());
    }

    public void actionPerformed(ActionEvent e) {
        paste();
        fireStatusPosted(Resources.ACTION_PASTE_COMMAND);
    }

    public void stateChanged(FontMapChangeEvent e) {
        setEnabled(!getSTEDWindow().getDesktop()
                .getClipboard()
                .isEmpty());
    }

    private void paste() {
        final STEDWindow stedWindow = getSTEDWindow();
        final Collection entries = stedWindow
                .getDesktop().getClipboard()
                .get(Resources.ENTRIES);
        if (entries != null && !entries.isEmpty()) {
            final FontMap fontMap =
                    stedWindow.getDesktop()
                            .getFontMap();
            final FontMapEntries fontMapEntries = fontMap.getEntries();
            boolean flag = false;
            for (final Object newVar : entries) {
                final FontMapEntry entry = (FontMapEntry) newVar;
                if (entry != null &&
                        fontMapEntries.add((FontMapEntry) entry.clone())) {
                    flag = true;
                }
            }
            fontMap.setDirty(flag);
            if (fontMap.isNew()) {
                stedWindow.getDesktop()
                        .getFontMapperDesktopFrame()
                        .getMapperPanel().getMappingEntryPanel
                        ().setFontMap(fontMap);
            } else {
                ((MappingTableModel) getTableModel()).setFontMap(fontMap);
            }
        }
    }

}
