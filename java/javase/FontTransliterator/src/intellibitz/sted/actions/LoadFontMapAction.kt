package intellibitz.sted.actions

import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.launch.STEDGUI
import java.awt.event.ActionEvent
import javax.swing.event.InternalFrameEvent
import javax.swing.event.InternalFrameListener
import javax.swing.event.TableModelEvent

class LoadFontMapAction : TableModelListenerAction(), FontMapChangeListener, InternalFrameListener {
    override fun tableChanged(e: TableModelEvent) {
        isEnabled = getSTEDWindow().desktop!!.fontMap!!.isReloadable()
    }

    override fun stateChanged(e: FontMapChangeEvent) {
        isEnabled = e.fontMap!!.isReloadable()
    }

    override fun actionPerformed(e: ActionEvent) {
        val stedWindow = getSTEDWindow()
        STEDGUI.busy()
        stedWindow.desktop!!.reloadFontMap()
        fireStatusPosted("FontMap Re-Loaded")
        STEDGUI.relax()
    }

    override fun internalFrameActivated(e: InternalFrameEvent) {}
    override fun internalFrameClosed(e: InternalFrameEvent) {}
    override fun internalFrameClosing(e: InternalFrameEvent) {
        isEnabled = false
    }

    override fun internalFrameDeactivated(e: InternalFrameEvent) {}
    override fun internalFrameDeiconified(e: InternalFrameEvent) {}
    override fun internalFrameIconified(e: InternalFrameEvent) {}
    override fun internalFrameOpened(e: InternalFrameEvent) {}
}
