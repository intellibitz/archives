package intellibitz.sted.event

import intellibitz.sted.fontmap.FontMapEntries
import javax.swing.event.ChangeEvent

class FontMapEntriesChangeEvent(fontMapEntries: FontMapEntries) : ChangeEvent(fontMapEntries) {
    val fontMapEntries: FontMapEntries
        get() = source as FontMapEntries
}
