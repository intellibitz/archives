package intellibitz.sted.actions

import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.fontmap.FontMap
import java.awt.event.ActionEvent
import javax.swing.event.TableModelEvent

class SaveFontMapAction : TableModelListenerAction(), FontMapChangeListener {
    
    override fun actionPerformed(e: ActionEvent) {
        val stedWindow = getSTEDWindow()
        stedWindow.desktop!!.saveAction()
        val fontMap = stedWindow.desktop!!.fontMap!!
        fontMap.fireUndoEvent()
        fontMap.fireRedoEvent()
        fireStatusPosted("Saved")
    }

    override fun stateChanged(e: FontMapChangeEvent) {
        isEnabled = isSaveable(e.fontMap!!)
    }

    override fun tableChanged(e: TableModelEvent) {
        val fontMap = getSTEDWindow().desktop!!.fontMap!!
        isEnabled = isSaveable(fontMap)
    }

    private fun isSaveable(fontMap: FontMap): Boolean {
        return fontMap.isDirty && fontMap.isFileWritable() || fontMap.isNew() && fontMap.isDirty
    }
}
