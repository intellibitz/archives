package intellibitz.sted.actions

import intellibitz.sted.ui.STEDWindow
import java.awt.event.ActionEvent

open class OpenFontMapAction : STEDWindowAction() {
    override fun actionPerformed(e: ActionEvent) {
        val stedWindow = getSTEDWindow()
        stedWindow.desktop!!.openFontMap()
    }
}
