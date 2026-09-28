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
 * $Id: STEDGUI.kt 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/launch/STEDGUI.kt $
 */

package intellibitz.sted.launch

import intellibitz.sted.ui.AboutText
import intellibitz.sted.ui.STEDWindow
import intellibitz.sted.util.Resources
import intellibitz.sted.widgets.SplashWindow
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Point
import java.awt.Toolkit
import java.io.File
import java.util.Iterator
import java.util.logging.Logger

class STEDGUI {
    private constructor() {
        var splashWindow: SplashWindow = SplashWindow(AboutText.getInstance())
        centerComponent(splashWindow)
        splashWindow.setVisible(true)
        STEDLogManager.getLogmanager()
        splashWindow.setProgress(10)
        stedWindow = STEDWindow()
        stedWindow.addStatusListener(splashWindow)
        stedWindow.init()
        stedWindow.load()
        splashWindow.setProgress(90)
        stedWindow.setVisible(true)
        val fileName: String = System.getProperty(Resources.FONTMAP_FILE)
        if (((null != fileName) && (Resources.EMPTY_STRING == fileName))) {
            stedWindow.getDesktop()
        }
        else {
            stedWindow.getDesktop()
        }
        splashWindow.setProgress(100)
        splashWindow.dispose()
    }

    companion object {
        private var logger: Logger = Logger.getLogger(STEDGUI::class.java)
        private var stedWindow: STEDWindow? = null
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                STEDGUI()
            }
            catch (e: Exception) {
                e.printStackTrace()
                logger.severe(("Exception : " + e.getMessage()))
                logger.throwing("intellibitz.sted.launch.STEDGUI", "main", e)
                System.exit(1)
            }
        }
        @JvmStatic
        fun getSTEDWindow(component: Component): STEDWindow {
            if ((null != stedWindow)) {
                return stedWindow
            }
            var parent: Component
            var src: Component = component
            do {
                parent = src.getParent()
                if (STEDWindow::class.java) {
                    stedWindow = (parent as STEDWindow)
                    return stedWindow
                }
                src = parent
            } while ((parent != null))
            return null
        }
        @JvmStatic
        fun getSTEDWindow(): STEDWindow {
            return stedWindow
        }
        @JvmStatic
        fun busy() {
            stedWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
        }
        @JvmStatic
        fun relax() {
            stedWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
        }
        @JvmStatic
        fun centerComponent(component: Component) {
            val dimension: Dimension = Toolkit.getDefaultToolkit()
            val size: Dimension = component.getSize()
            component.setLocation(Point(((dimension.width - size.width) / 2), ((dimension.height - size.height) / 2)))
        }
        @Throws(ClassNotFoundException::class, InstantiationException::class, IllegalAccessException::class, UnsupportedLookAndFeelException::class)
        @JvmStatic
        fun updateUIWithLAF(lookAndFeel: String, iterator: Iterator<Component>) {
            UIManager.setLookAndFeel(lookAndFeel)
            while (iterator.hasNext()) {
                val component: Component = iterator.next()
                SwingUtilities.updateComponentTreeUI(component)
            }
        }
    }
}
