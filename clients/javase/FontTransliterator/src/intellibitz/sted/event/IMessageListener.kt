package intellibitz.sted.event

import java.util.EventListener

interface IMessageListener : EventListener {
    fun messagePosted(event: MessageEvent)
}
