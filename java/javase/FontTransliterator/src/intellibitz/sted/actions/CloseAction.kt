package intellibitz.sted.actions

import javax.swing.event.InternalFrameEvent
import javax.swing.event.InternalFrameListener
import java.awt.event.ActionEvent

class CloseAction : STEDWindowAction(), InternalFrameListener {
    override fun actionPerformed(e: ActionEvent) {
        getSTEDWindow().desktop!!.closeFontMap()
        fireStatusPosted("FontMap closed")
    }

    override fun internalFrameActivated(e: InternalFrameEvent) {
        isEnabled = true
    }

    override fun internalFrameClosed(e: InternalFrameEvent) {}
    override fun internalFrameClosing(e: InternalFrameEvent) {
        isEnabled = false
    }

    override fun internalFrameDeactivated(e: InternalFrameEvent) {}
    override fun internalFrameDeiconified(e: InternalFrameEvent) {}
    override fun internalFrameIconified(e: InternalFrameEvent) {}
    override fun internalFrameOpened(e: InternalFrameEvent) {}
}
