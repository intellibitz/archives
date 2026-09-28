package intellibitz.sted.actions

import intellibitz.sted.ui.FontKeypad
import intellibitz.sted.util.FileHelper
import intellibitz.sted.util.Resources
import java.awt.event.ActionEvent
import javax.swing.Action

class LoadFontAction(private val fontKeypad: FontKeypad) : STEDWindowAction() {
    init {
        putValue(Action.NAME, Resources.getSetting(Resources.LABEL_FONT_LOAD))
    }

    override fun actionPerformed(e: ActionEvent) {
        loadFont()
    }

    private fun loadFont() {
        val file = FileHelper.openFont(getSTEDWindow())
        if (file != null) {
            fontKeypad.loadFont(file)
        }
    }
}
