package intellibitz.sted.event

import java.util.EventListener

interface IStatusListener : EventListener {
    fun statusPosted(event: StatusEvent)
}
