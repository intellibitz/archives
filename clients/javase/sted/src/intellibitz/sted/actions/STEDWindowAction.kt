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
 * $Id:STEDWindowAction.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/STEDWindowAction.kt $
 */

package intellibitz.sted.actions


import intellibitz.sted.event.IMessageEventSource
import intellibitz.sted.event.IMessageListener
import intellibitz.sted.event.IStatusEventSource
import intellibitz.sted.event.IStatusListener
import intellibitz.sted.event.MessageEvent
import intellibitz.sted.event.StatusEvent
import intellibitz.sted.launch.STEDGUI
import intellibitz.sted.ui.STEDWindow
import javax.swing.AbstractAction
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener
import java.awt.event.WindowListener
import java.awt.event.WindowStateListener
import java.util.logging.Logger

class STEDWindowAction : AbstractAction() {
    private var stedWindow: STEDWindow? = null
    protected var logger: Logger? = null
    private var statusEvent: StatusEvent? = null
    private var statusListener: IStatusListener? = null
    private var messageEvent: MessageEvent? = null
    private var messageListener: IMessageListener? = null
    /**
         * Invoked when a window has been opened.
         */
    /**
         * Invoked when a window is in the process of being closed. The close
         * operation can be overridden at this point.
         */
    /**
         * Invoked when a window has been closed.
         */
    /**
         * Invoked when a window is iconified.
         */
    /**
         * Invoked when a window is de-iconified.
         */
    /**
         * Invoked when a window is activated.
         */
    /**
         * Invoked when a window is de-activated.
         */
    /**
         * Invoked when a window state is changed.
         *
         * @since 1.4
         */
    /**
         * Invoked when the Window is set to be the focused Window, which means that
         * the Window, or one of its subcomponents, will receive keyboard events.
         *
         * @since 1.4
         */
    /**
         * Invoked when the Window is no longer the focused Window, which means that
         * keyboard events will no longer be delivered to the Window or any of its
         * subcomponents.
         *
         * @since 1.4
         */
    constructor() : super() {
            logger = Logger.getLogger(getClass().getName())
            statusEvent = StatusEvent(this)
            messageEvent = MessageEvent(this)
        }
    fun fireMessagePosted(message: String) {
            messageEvent.setMessage(message)
            messageListener.messagePosted(messageEvent)
        }
    fun fireMessagePosted() {
            messageListener.messagePosted(messageEvent)
        }
    fun addMessageListener(messageListener: IMessageListener) {
            this.messageListener = messageListener
        }
    fun fireStatusPosted(message: String) {
            statusEvent.setStatus(message)
            statusListener.statusPosted(statusEvent)
        }
    fun fireStatusPosted() {
            statusListener.statusPosted(statusEvent)
        }
    fun addStatusListener(statusListener: IStatusListener) {
            this.statusListener = statusListener
        }
    override fun actionPerformed(e: ActionEvent) {
    
        
        }
    fun getSTEDWindow(): STEDWindow {
            if (null == stedWindow)
            {
                stedWindow = STEDGUI.getSTEDWindow()
            }
            return stedWindow
        }
    fun setSTEDWindow(stedWindow: STEDWindow) {
            this.stedWindow = stedWindow
        }
    protected fun showMessageDialog(message: String) {
            fireMessagePosted(message)
        }
    override fun windowOpened(e: WindowEvent) {
        
        }
    override fun windowClosing(e: WindowEvent) {
        
        }
    override fun windowClosed(e: WindowEvent) {
        
        }
    override fun windowIconified(e: WindowEvent) {
        
        }
    override fun windowDeiconified(e: WindowEvent) {
        
        }
    override fun windowActivated(e: WindowEvent) {
        
        }
    override fun windowDeactivated(e: WindowEvent) {
        
        }
    override fun windowStateChanged(e: WindowEvent) {
        
        }
    override fun windowGainedFocus(e: WindowEvent) {
        
        }
    override fun windowLostFocus(e: WindowEvent) {
        
        }
    override fun keyTyped(e: KeyEvent) {
            //To change body of implemented methods use File | Settings | File Templates.
        
        }
    override fun keyPressed(e: KeyEvent) {
            //To change body of implemented methods use File | Settings | File Templates.
        
        }
    override fun keyReleased(e: KeyEvent) {
            //To change body of implemented methods use File | Settings | File Templates.
        
        }
}
