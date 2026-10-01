package intellibitz.sted.event

import java.util.EventListener

interface FontMapChangeListener : EventListener {
    fun stateChanged(e: FontMapChangeEvent)
}
