package intellibitz.sted.event

import java.util.EventListener

interface IFontMapEntriesChangeListener : EventListener {
    fun stateChanged(e: FontMapEntriesChangeEvent)
}
