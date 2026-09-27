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
 * $Id:CopyAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/CopyAction.java $
 */

/**
 * $Id:CopyAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/CopyAction.java $
 */

package intellibitz.sted.actions;

import intellibitz.sted.event.FontMapChangeEvent;
import intellibitz.sted.event.FontMapChangeListener;
import intellibitz.sted.ui.STEDWindow;
import intellibitz.sted.util.Resources;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import java.awt.event.ActionEvent;

public class CopyAction
        extends TableModelListenerAction
        implements FontMapChangeListener {
    public CopyAction() {
        super();
    }


    /**
     * This fine grain notification tells listeners the exact range of cells,
     * rows, or columns that changed.
     */
    public void tableChanged(TableModelEvent e) {
//        this.setEnabled(!this.getSelectedRows().isEmpty());
    }

    public void valueChanged(ListSelectionEvent e) {
        final ListSelectionModel listSelectionModel =
                (ListSelectionModel) e.getSource();
        setEnabled(listSelectionModel.getMinSelectionIndex() >= 0);
    }

    public void actionPerformed(ActionEvent e) {
        final STEDWindow stedWindow = getSTEDWindow();
        stedWindow.getDesktop()
                .addToClipboard(Resources.ENTRIES, copySelectedRows());
    }

    public void stateChanged(FontMapChangeEvent e) {
        setEnabled(!getSelectedRows().isEmpty());
    }

}
