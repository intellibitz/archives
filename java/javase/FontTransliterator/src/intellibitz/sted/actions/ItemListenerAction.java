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
 * $Id:ItemListenerAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/ItemListenerAction.java $
 */

/**
 * $Id:ItemListenerAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/ItemListenerAction.java $
 */

package intellibitz.sted.actions;

import intellibitz.sted.ui.STEDWindow;
import intellibitz.sted.util.MenuHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ItemListenerAction
        extends STEDWindowAction
        implements ItemListener {
    public ItemListenerAction() {
        super();
    }

    public void actionPerformed(ActionEvent e) {
    }

    /**
     * Invoked when an item has been selected or deselected by the user. The
     * code written for this method performs the operations that need to occur
     * when an item is selected (or deselected).
     */
    public void itemStateChanged(ItemEvent e) {
        boolean state = true;
        if (ItemEvent.DESELECTED == e.getStateChange()) {
            state = false;
        }
        final Object object = e.getSource();
        final Action action;
        if (AbstractButton.class.isInstance(object)) {
            final STEDWindow stedWindow = getSTEDWindow();
            action = ((AbstractButton) object).getAction();
            AbstractButton button = MenuHandler.getInstance()
                    .getMenuItem((String) action.getValue(Action.NAME));
            if (button != null) {
                button.setSelected(state);
            }
            button = MenuHandler.getInstance()
                    .getToolButton((String) action.getValue(Action.NAME));
            if (button != null) {
                button.setSelected(state);
            }
//            getSTEDWindow().convertSampleText();
        }
    }

}
