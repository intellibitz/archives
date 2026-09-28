package intellibitz.sted.ui

import intellibitz.sted.event.FontListChangeEvent
import intellibitz.sted.fontmap.FontMap
import java.io.File
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class FontKeypad2 : FontKeypad(), ChangeListener {
    override fun loadFont(font: File) {
        val fm = fontMap
        if (fm != null) {
            fm.font2 = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, font)
        }
    }

    override fun setCurrentFont() {
        super.setCurrentFont(fontMap?.font2)
    }

    override fun setCurrentFont(font: String) {
        super.setCurrentFont(font)
        val fm = fontMap
        if (fm != null) {
            fm.font2 = currentFont
        }
    }

    override fun stateChanged(e: ChangeEvent) {
        if ((e as FontListChangeEvent).fontIndex == 2) {
            fontSelector.stateChanged(e)
            updateUI()
        }
    }
}
