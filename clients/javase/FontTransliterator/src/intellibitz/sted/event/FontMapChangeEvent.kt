package intellibitz.sted.event

import intellibitz.sted.fontmap.FontMap
import javax.swing.event.ChangeEvent

class FontMapChangeEvent(fontMap: FontMap) : ChangeEvent(fontMap) {
    val fontMap: FontMap
        get() = source as FontMap
}
