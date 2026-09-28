package intellibitz.sted.actions

import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import java.awt.event.ActionEvent
import javax.swing.event.InternalFrameEvent
import javax.swing.event.InternalFrameListener

class NewFontMapAction : STEDWindowAction(), FontMapChangeListener, InternalFrameListener {
    override fun actionPerformed(e: ActionEvent) {
        val tabDesktop = getSTEDWindow().desktop!!
        tabDesktop.newFontMap()
    }

    override fun stateChanged(fontMapChangeEvent: FontMapChangeEvent) {
        val fontMap = fontMapChangeEvent.fontMap
        if (fontMap == null) {
            isEnabled = true
        } else {
            if (fontMap.isNew()) {
                isEnabled = fontMap.isDirty
            } else {
                isEnabled = true
            }
        }
        isEnabled = true
    }

    override fun internalFrameActivated(e: InternalFrameEvent) {}
    override fun internalFrameClosed(e: InternalFrameEvent) {}
    override fun internalFrameClosing(e: InternalFrameEvent) {
        isEnabled = true
    }

    override fun internalFrameDeactivated(e: InternalFrameEvent) {
        isEnabled = true
    }

    override fun internalFrameDeiconified(e: InternalFrameEvent) {}
    override fun internalFrameIconified(e: InternalFrameEvent) {}
    override fun internalFrameOpened(e: InternalFrameEvent) {}
}
