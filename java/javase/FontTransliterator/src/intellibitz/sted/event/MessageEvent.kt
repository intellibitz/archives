package intellibitz.sted.event

import java.util.EventObject

open class MessageEvent(src: IMessageEventSource) : EventObject(src), Cloneable {
    val eventSource: IMessageEventSource = src
    var message: String? = null

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }
}
