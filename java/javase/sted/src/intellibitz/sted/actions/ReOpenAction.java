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
 * $Id:ReOpenAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/ReOpenAction.java $
 */

package intellibitz.sted.actions;

import intellibitz.sted.event.FontMapChangeEvent;
import intellibitz.sted.event.FontMapChangeListener;
import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.ui.STEDWindow;
import intellibitz.sted.util.MenuHandler;
import intellibitz.sted.util.Resources;

import javax.swing.JMenu;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.event.ActionEvent;

/**
 * Listens to STEDWindow for any FontMap change event. Listens to
 * FontMapperDesktop for InternalFrame events.
 */
public class ReOpenAction
        extends ReOpenFontMapAction
        implements FontMapChangeListener,
        InternalFrameListener
{
    public ReOpenAction()
    {
        super();
    }

    public void actionPerformed(ActionEvent e)
    {
    }

    public void stateChanged(FontMapChangeEvent e)
    {
        final FontMap fontMap = e.getFontMap();
        if (fontMap.isNew())
        {
            MenuHandler.enableReOpenItems(MenuHandler.getInstance());
        }
        else
        {
            final String fileName = fontMap.getFileName();
            final STEDWindow stedWindow = getSTEDWindow();
            stedWindow.getDesktop()
                    .addItemToReOpenMenu(fileName);
            // needed when opening a new fontmap
            MenuHandler.disableMenuItem(MenuHandler.getInstance(), fileName);
        }
    }

    /**
     * Invoked when an internal frame is activated.
     *
     * @see javax.swing.JInternalFrame#setSelected
     */
    public void internalFrameActivated(InternalFrameEvent e)
    {
        MenuHandler.enableItemsInReOpenMenu(MenuHandler.getInstance(),
                getSTEDWindow().getDesktop()
                        .getFontMap());
    }

    /**
     * Invoked when a internal frame has been opened.
     *
     * @see javax.swing.JInternalFrame#show
     */
    public void internalFrameOpened(InternalFrameEvent e)
    {
        MenuHandler.enableItemsInReOpenMenu(MenuHandler.getInstance(),
                getSTEDWindow()
                        .getDesktop()
                        .getFontMap());
    }


    /**
     * Invoked when an internal frame is de-activated.
     *
     * @see javax.swing.JInternalFrame#setSelected
     */
    public void internalFrameDeactivated(InternalFrameEvent e)
    {
        addItemToReOpenMenu();
    }

    /**
     * Invoked when an internal frame is in the process of being closed. The
     * close operation can be overridden at this point.
     *
     * @see javax.swing.JInternalFrame#setDefaultCloseOperation
     */
    public void internalFrameClosing(InternalFrameEvent e)
    {
        addItemToReOpenMenu();
    }

    /**
     * Invoked when an internal frame has been closed.
     *
     * @see javax.swing.JInternalFrame#setClosed
     */
    public void internalFrameClosed(InternalFrameEvent e)
    {
    }

    /**
     * Invoked when an internal frame is de-iconified.
     *
     * @see javax.swing.JInternalFrame#setIcon
     */
    public void internalFrameDeiconified(InternalFrameEvent e)
    {

    }

    /**
     * Invoked when an internal frame is iconified.
     *
     * @see javax.swing.JInternalFrame#setIcon
     */
    public void internalFrameIconified(InternalFrameEvent e)
    {

    }

    public void addItemToReOpenMenu()
    {
        final STEDWindow stedWindow = getSTEDWindow();
        final MenuHandler menuHandler = MenuHandler.getInstance();
        final FontMap fontMap = stedWindow.getDesktop()
                .getFontMap();
        final JMenu menu =
                menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND);
        if (!fontMap.isNew())
        {
            MenuHandler.addReOpenItem(menu, fontMap.getFileName());
        }
        MenuHandler.enableReOpenItems(menu);
    }


}
