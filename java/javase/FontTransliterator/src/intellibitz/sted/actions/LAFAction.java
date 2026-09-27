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
 * $Id:LAFAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/LAFAction.java $
 * <p>
 * sted actions package
 */

/**
 * $Id:LAFAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/LAFAction.java $
 */

/**
 * sted actions package
 */
package intellibitz.sted.actions;

import intellibitz.sted.launch.STEDGUI;
import intellibitz.sted.ui.AboutSTED;
import intellibitz.sted.ui.HelpWindow;
import intellibitz.sted.ui.STEDWindow;
import intellibitz.sted.util.MenuHandler;
import intellibitz.sted.util.Resources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class LAFAction
        extends STEDWindowAction {
    private static final Logger logger =
            Logger.getLogger("intellibitz.sted.actions.LAFAction");

    public LAFAction() {
        super();
    }

    public void actionPerformed(ActionEvent e) {
        try {
            final Collection<Component> collection = new ArrayList<Component>();
            final STEDWindow stedWindow = getSTEDWindow();
            collection.add(stedWindow);
            final HelpWindow help = HelpWindow.getInstance();
            if (null != help) {
                collection.add(help);
            }
            final AboutSTED aboutDialog = AboutSTED.getInstance();
            if (null != aboutDialog) {
                collection.add(aboutDialog);
            }
            final Component component = MenuHandler.getInstance()
                    .getPopupMenu(Resources.MENU_POPUP_MAPPING);
            if (null != component) {
                collection.add(component);
            }
            STEDGUI.updateUIWithLAF(e.getActionCommand(),
                    collection.iterator());
        } catch (ClassNotFoundException e1) {
            logger.throwing(getClass().getName(), "actionPerformed", e1);
            fireMessagePosted("Unable to Set LookAndFeel - Class Not Found: " +
                    e1.getMessage());
            e1.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } catch (InstantiationException e1) {
            logger.throwing(getClass().getName(), "actionPerformed", e1);
            fireMessagePosted(
                    "Unable to Set LookAndFeel - Cannot Instantiate: " +
                            e1.getMessage());
            e1.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } catch (IllegalAccessException e1) {
            logger.throwing(getClass().getName(), "actionPerformed", e1);
            fireMessagePosted("Unable to Set LookAndFeel - IllegalAccess: " +
                    e1.getMessage());
            e1.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } catch (UnsupportedLookAndFeelException e1) {
            logger.throwing(getClass().getName(), "actionPerformed", e1);
            fireMessagePosted("Unable to Set LookAndFeel - Unsupported: " +
                    e1.getMessage());
            e1.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
    }

}
