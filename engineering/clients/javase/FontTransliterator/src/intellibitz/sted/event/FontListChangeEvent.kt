package intellibitz.sted.event

import intellibitz.sted.fontmap.FontMap
import javax.swing.event.ChangeEvent
import java.awt.Font

class FontListChangeEvent(fontMap: FontMap) : ChangeEvent(fontMap) {
    var fontChanged: Font? = null
    var fontIndex: Int = 0
}
