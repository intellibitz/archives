package intellibitz.sted.event

import java.util.EventObject

open class KeypadEvent(src: IKeypadEventSource) : EventObject(src), Cloneable {
    val eventSource: IKeypadEventSource = src

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }
}
