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
 * $Id: STEDGUI.java 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/launch/STEDGUI.java $
 */

/**
 * $Id: STEDGUI.java 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/launch/STEDGUI.java $
 */

package intellibitz.sted.launch;

import intellibitz.sted.ui.AboutText;
import intellibitz.sted.ui.STEDWindow;
import intellibitz.sted.util.Resources;
import intellibitz.sted.widgets.SplashWindow;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * STED GUI The Root of STED GUI Application
 */
public class STEDGUI {
    private static Logger logger = Logger.getLogger(STEDGUI.class.getName());
    private static STEDWindow stedWindow;


    private STEDGUI() {
        SplashWindow splashWindow = new SplashWindow(AboutText.getInstance());
        centerComponent(splashWindow);
        splashWindow.setVisible(true);
        STEDLogManager.getLogmanager().addLogger(logger);
        splashWindow.setProgress(10);

        stedWindow = new STEDWindow();
        stedWindow.addStatusListener(splashWindow);
        stedWindow.init();
        stedWindow.load();
        splashWindow.setProgress(90);
        stedWindow.setVisible(true);
        final String fileName = System.getProperty(Resources.FONTMAP_FILE);
        if (null != fileName && !Resources.EMPTY_STRING.equals(fileName)) {
            stedWindow.getDesktop()
                    .openFontMap(new File(fileName));
        } else {
//            File file = new File(Resources.getSampleFontMap());
//            stedWindow.getDesktop().openFontMap(file);
            stedWindow.getDesktop().newFontMap();
        }
        splashWindow.setProgress(100);
        splashWindow.dispose();
    }


    public static void main(String[] args) {
        try {
            new STEDGUI();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            logger.severe("Exception : " + e.getMessage());
            logger.throwing("intellibitz.sted.launch.STEDGUI", "main", e);
            System.exit(-1);
        }
    }


    public static STEDWindow getSTEDWindow(Component component) {
        // if STEDWindow is already found.. return from cache
        if (null != stedWindow) {
            return stedWindow;
        }
        Component parent;
        Component src = component;
        do {
            parent = src.getParent();
            if (STEDWindow.class.isInstance(parent)) {
                stedWindow =
                        (STEDWindow) parent;
                return stedWindow;
            }
            src = parent;
        }
        while (parent != null);
        return null;
    }

    public static STEDWindow getSTEDWindow() {
        return stedWindow;
    }

    public static void busy() {
        stedWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    public static void relax() {
        stedWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * A very nice trick is to center windows on screen
     *
     * @param component The <code>Component</code> to center
     */

    public static void centerComponent(Component component) {
        final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension size = component.getSize();
        component.setLocation(new Point((dimension.width - size.width) / 2,
                (dimension.height - size.height) / 2));
    }

    public static void updateUIWithLAF(String lookAndFeel,
                                       Iterator<Component> iterator)
            throws ClassNotFoundException,
            InstantiationException,
            IllegalAccessException,
            UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(lookAndFeel);
        while (iterator.hasNext()) {
            final Component component = iterator.next();
            SwingUtilities.updateComponentTreeUI(component);
        }
    }

}
