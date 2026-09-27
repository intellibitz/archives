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
 * $Id:STEDWindowAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/STEDWindowAction.java $
 */

/**
 * $Id:STEDWindowAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/STEDWindowAction.java $
 */

package intellibitz.sted.actions;

import intellibitz.sted.event.*;
import intellibitz.sted.launch.STEDGUI;
import intellibitz.sted.ui.STEDWindow;

import javax.swing.*;
import java.awt.event.*;
import java.util.logging.Logger;

public class STEDWindowAction
        extends AbstractAction
        implements WindowListener,
        KeyListener,
        WindowStateListener,
        WindowFocusListener,
        IStatusEventSource,
        IMessageEventSource {

    private STEDWindow stedWindow;
    protected Logger logger;
    private StatusEvent statusEvent;
    private IStatusListener statusListener;
    private MessageEvent messageEvent;
    private IMessageListener messageListener;

    public STEDWindowAction() {
        super();
        logger = Logger.getLogger(getClass().getName());
        statusEvent = new StatusEvent(this);
        messageEvent = new MessageEvent(this);
    }

    public void fireMessagePosted(String message) {
        messageEvent.setMessage(message);
        messageListener.messagePosted(messageEvent);
    }

    public void fireMessagePosted() {
        messageListener.messagePosted(messageEvent);
    }

    public void addMessageListener(IMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void fireStatusPosted(String message) {
        statusEvent.setStatus(message);
        statusListener.statusPosted(statusEvent);
    }

    public void fireStatusPosted() {
        statusListener.statusPosted(statusEvent);
    }

    public void addStatusListener(IStatusListener statusListener) {
        this.statusListener = statusListener;
    }

    public void actionPerformed(ActionEvent e) {

    }

    public STEDWindow getSTEDWindow() {
        if (null == stedWindow) {
            stedWindow = STEDGUI.getSTEDWindow();
        }
        return stedWindow;
    }

    public void setSTEDWindow(STEDWindow stedWindow) {
        this.stedWindow = stedWindow;
    }

    protected void showMessageDialog(String message) {
        fireMessagePosted(message);
    }

    /**
     * Invoked when a window has been opened.
     */
    public void windowOpened(WindowEvent e) {
    }

    /**
     * Invoked when a window is in the process of being closed. The close
     * operation can be overridden at this point.
     */
    public void windowClosing(WindowEvent e) {
    }

    /**
     * Invoked when a window has been closed.
     */
    public void windowClosed(WindowEvent e) {
    }

    /**
     * Invoked when a window is iconified.
     */
    public void windowIconified(WindowEvent e) {
    }

    /**
     * Invoked when a window is de-iconified.
     */
    public void windowDeiconified(WindowEvent e) {
    }

    /**
     * Invoked when a window is activated.
     */
    public void windowActivated(WindowEvent e) {
    }

    /**
     * Invoked when a window is de-activated.
     */
    public void windowDeactivated(WindowEvent e) {
    }

    /**
     * Invoked when a window state is changed.
     *
     * @since 1.4
     */
    public void windowStateChanged(WindowEvent e) {
    }

    /**
     * Invoked when the Window is set to be the focused Window, which means that
     * the Window, or one of its subcomponents, will receive keyboard events.
     *
     * @since 1.4
     */
    public void windowGainedFocus(WindowEvent e) {
    }

    /**
     * Invoked when the Window is no longer the focused Window, which means that
     * keyboard events will no longer be delivered to the Window or any of its
     * subcomponents.
     *
     * @since 1.4
     */
    public void windowLostFocus(WindowEvent e) {
    }

    public void keyTyped(KeyEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void keyPressed(KeyEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void keyReleased(KeyEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
