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
 * $Id:ReOpenAction.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/ReOpenAction.kt $
 */

package intellibitz.sted.actions


import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.ui.STEDWindow
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import javax.swing.JMenu
import javax.swing.event.InternalFrameEvent
import javax.swing.event.InternalFrameListener
import java.awt.event.ActionEvent

class ReOpenAction : ReOpenFontMapAction() {
    /**
         * Invoked when an internal frame is activated.
         *
         * @see javax.swing.JInternalFrame#setSelected
         */
    /**
         * Invoked when a internal frame has been opened.
         *
         * @see javax.swing.JInternalFrame#show
         */
    /**
         * Invoked when an internal frame is de-activated.
         *
         * @see javax.swing.JInternalFrame#setSelected
         */
    /**
         * Invoked when an internal frame is in the process of being closed. The
         * close operation can be overridden at this point.
         *
         * @see javax.swing.JInternalFrame#setDefaultCloseOperation
         */
    /**
         * Invoked when an internal frame has been closed.
         *
         * @see javax.swing.JInternalFrame#setClosed
         */
    /**
         * Invoked when an internal frame is de-iconified.
         *
         * @see javax.swing.JInternalFrame#setIcon
         */
    /**
         * Invoked when an internal frame is iconified.
         *
         * @see javax.swing.JInternalFrame#setIcon
         */
    constructor() : super()
    override fun actionPerformed(e: ActionEvent) {
        
        }
    override fun stateChanged(e: FontMapChangeEvent) {
            val fontMap: FontMap = e.getFontMap()
            if (fontMap.isNew())
            {
                MenuHandler.enableReOpenItems(MenuHandler.getInstance())
            }
            else
            {
                val fileName: String = fontMap.getFileName()
                val stedWindow: STEDWindow = getSTEDWindow()
                stedWindow.getDesktop()
                        .addItemToReOpenMenu(fileName)
                // needed when opening a new fontmap
                MenuHandler.disableMenuItem(MenuHandler.getInstance(), fileName)
            }
        
        }
    fun internalFrameActivated(e: InternalFrameEvent) {
            MenuHandler.enableItemsInReOpenMenu(MenuHandler.getInstance(),
                    getSTEDWindow().getDesktop()
                            .getFontMap())
        }
    fun internalFrameOpened(e: InternalFrameEvent) {
            MenuHandler.enableItemsInReOpenMenu(MenuHandler.getInstance(),
                    getSTEDWindow()
                            .getDesktop()
                            .getFontMap())
        }
    fun internalFrameDeactivated(e: InternalFrameEvent) {
            addItemToReOpenMenu()
        }
    fun internalFrameClosing(e: InternalFrameEvent) {
            addItemToReOpenMenu()
        }
    fun internalFrameClosed(e: InternalFrameEvent) {
        
        }
    fun internalFrameDeiconified(e: InternalFrameEvent) {
    
        
        }
    fun internalFrameIconified(e: InternalFrameEvent) {
    
        
        }
    fun addItemToReOpenMenu() {
            val stedWindow: STEDWindow = getSTEDWindow()
            val menuHandler: MenuHandler = MenuHandler.getInstance()
            val fontMap: FontMap = stedWindow.getDesktop()
                    .getFontMap()
            val menu: JMenu =
                    menuHandler.getMenu(if as Resources.ACTION_FILE_REOPEN_COMMAND) (!fontMap.isNew())
            {
                MenuHandler.addReOpenItem(menu, fontMap.getFileName())
            }
            MenuHandler.enableReOpenItems(menu)
        }
}
