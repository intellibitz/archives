package intellibitz.sted.event

import java.util.EventObject

open class ThreadEvent(runnable: IThreadEventSource) : EventObject(runnable), Cloneable {
    val eventSource: IThreadEventSource = runnable

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }
}
