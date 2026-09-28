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
import java.awt.event.*
import java.util.logging.Logger

open class STEDWindowAction : AbstractAction(), WindowListener, KeyListener, WindowStateListener, WindowFocusListener, IStatusEventSource, IMessageEventSource {

    private var stedWindow: STEDWindow? = null
    protected val logger: Logger = Logger.getLogger(javaClass.name)
    private val statusEvent: StatusEvent = StatusEvent(this)
    private var statusListener: IStatusListener? = null
    private val messageEvent: MessageEvent = MessageEvent(this)
    private var messageListener: IMessageListener? = null

    fun fireMessagePosted(message: String) {
        messageEvent.message = message
        messageListener?.messagePosted(messageEvent)
    }

    override fun fireMessagePosted() {
        messageListener?.messagePosted(messageEvent)
    }

    override fun addMessageListener(messageListener: IMessageListener) {
        this.messageListener = messageListener
    }

    fun fireStatusPosted(message: String) {
        statusEvent.status = message
        statusListener?.statusPosted(statusEvent)
    }

    override fun fireStatusPosted() {
        statusListener?.statusPosted(statusEvent)
    }

    override fun addStatusListener(statusListener: IStatusListener) {
        this.statusListener = statusListener
    }

    override fun actionPerformed(e: ActionEvent) {}

    fun getSTEDWindow(): STEDWindow {
        if (stedWindow == null) {
            stedWindow = STEDGUI.stedWindow!!
        }
        return stedWindow!!
    }

    fun setSTEDWindow(stedWindow: STEDWindow) {
        this.stedWindow = stedWindow
    }

    protected fun showMessageDialog(message: String) {
        fireMessagePosted(message)
    }

    override fun windowOpened(e: WindowEvent) {}
    override fun windowClosing(e: WindowEvent) {}
    override fun windowClosed(e: WindowEvent) {}
    override fun windowIconified(e: WindowEvent) {}
    override fun windowDeiconified(e: WindowEvent) {}
    override fun windowActivated(e: WindowEvent) {}
    override fun windowDeactivated(e: WindowEvent) {}
    override fun windowStateChanged(e: WindowEvent) {}
    override fun windowGainedFocus(e: WindowEvent) {}
    override fun windowLostFocus(e: WindowEvent) {}
    override fun keyTyped(e: KeyEvent) {}
    override fun keyPressed(e: KeyEvent) {}
    override fun keyReleased(e: KeyEvent) {}
}
