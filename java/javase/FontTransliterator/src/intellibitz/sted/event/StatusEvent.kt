package intellibitz.sted.event

import java.util.EventObject

open class StatusEvent(src: IStatusEventSource) : EventObject(src), Cloneable {
    val eventSource: IStatusEventSource = src
    var status: String? = null

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }
}
