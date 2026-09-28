package intellibitz.sted.actions

import intellibitz.sted.launch.STEDGUI
import java.awt.event.ActionEvent
import javax.swing.Action

class OpenSampleFontMapAction : STEDWindowAction() {
    override fun actionPerformed(e: ActionEvent) {
        val stedWindow = getSTEDWindow()
        STEDGUI.busy()
        val tabDesktop = stedWindow.desktop!!
        val fileName = getValue(Action.NAME) as String
        tabDesktop.reopenFontMap(fileName)
        STEDGUI.relax()
    }
}
