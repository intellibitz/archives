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
 * $Id:ViewAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/ViewAction.java $
 * <p>
 * sted actions package
 */

/**
 * $Id:ViewAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/ViewAction.java $
 */

/**
 * sted actions package
 */
package intellibitz.sted.actions;

import intellibitz.sted.util.MenuHandler;
import intellibitz.sted.util.Resources;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class ViewAction
        extends ItemListenerAction {
    public ViewAction() {
        super();
    }

    static public class ViewSample
            extends ViewAction {
        public ViewSample() {
            super();
        }

        public void itemStateChanged(ItemEvent e) {
            final JPanel sampleText = getSTEDWindow().getDesktop()

                    .getFontMapperDesktopFrame()
                    .getMapperPanel()
                    .getPreviewPanel();
            sampleText.setVisible(ItemEvent.SELECTED == e.getStateChange());
            sampleText.validate();
        }
    }

    static public class ViewToolBar
            extends ViewAction {
        public ViewToolBar() {
            super();
        }

        public void itemStateChanged(ItemEvent e) {
            MenuHandler.getInstance().getToolBar(Resources.MENUBAR_STED)
                    .setVisible(
                            ItemEvent.SELECTED == e.getStateChange());
        }
    }

    static public class ViewStatus
            extends ViewAction {
        public ViewStatus() {
            super();
        }

        public void itemStateChanged(ItemEvent e) {
            getSTEDWindow().getStatusPanel()
                    .setVisible(ItemEvent.SELECTED == e.getStateChange());
        }
    }

    static public class ViewMapping
            extends ViewAction {
        public ViewMapping() {
            super();
        }

        public void itemStateChanged(ItemEvent e) {
            final JSplitPane splitPane = getSTEDWindow()
                    .getDesktop()
                    .getFontMapperDesktopFrame()
                    .getMapperPanel
                            ().getMappingEntryPanel().getSplitPane();
            splitPane.getBottomComponent().setVisible
                    (ItemEvent.SELECTED == e.getStateChange());
            splitPane.resetToPreferredSizes();
            splitPane.validate();
        }
    }

}
