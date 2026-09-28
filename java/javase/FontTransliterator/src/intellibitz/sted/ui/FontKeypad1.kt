package intellibitz.sted.ui

import intellibitz.sted.event.FontListChangeEvent
import intellibitz.sted.fontmap.FontMap
import java.io.File
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class FontKeypad1 : FontKeypad(), ChangeListener {
    override fun setCurrentFont() {
        super.setCurrentFont(fontMap?.font1)
    }

    override fun loadFont(font: File) {
        val fm = fontMap
        if (fm != null) {
            fm.font1 = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, font)
        }
    }

    override fun setCurrentFont(font: String) {
        super.setCurrentFont(font)
        val fm = fontMap
        if (fm != null) {
            fm.font1 = currentFont
        }
    }

    override fun stateChanged(e: ChangeEvent) {
        if ((e as FontListChangeEvent).fontIndex == 1) {
            fontSelector.stateChanged(e)
            updateUI()
        }
    }
}
