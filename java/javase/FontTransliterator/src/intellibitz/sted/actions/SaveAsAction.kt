package intellibitz.sted.actions

import java.awt.event.ActionEvent
import javax.swing.JFileChooser
import javax.swing.event.InternalFrameEvent
import javax.swing.event.InternalFrameListener

class SaveAsAction : TableModelListenerAction(), InternalFrameListener {

    override fun actionPerformed(e: ActionEvent) {
        val stedWindow = getSTEDWindow()
        if (JFileChooser.ERROR_OPTION == stedWindow.desktop!!.saveAsAction()) {
            fireMessagePosted("Cannot Save.. Error" + JFileChooser.ERROR_OPTION)
            return
        }
        val fontMap = stedWindow.desktop!!.fontMap!!
        fontMap.fireUndoEvent()
        fontMap.fireRedoEvent()
        fireStatusPosted("Saved")
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
