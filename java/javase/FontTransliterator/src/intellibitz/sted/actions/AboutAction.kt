package intellibitz.sted.actions

import intellibitz.sted.ui.AboutSTED
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

class AboutAction : AbstractAction() {
    override fun actionPerformed(e: ActionEvent) {
        AboutSTED.instance!!.isVisible = true
    }
}
