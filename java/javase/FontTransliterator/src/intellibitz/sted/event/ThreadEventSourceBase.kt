package intellibitz.sted.event

import javax.swing.event.EventListenerList

open class ThreadEventSourceBase : Thread(), IThreadEventSource {
    private val eventListenerList = EventListenerList()
    var threadEvent: ThreadEvent? = null
        protected set
    override var message: Any? = null
    override var result: Any? = null
    override var progress: Int = 0
    override var progressMaximum: Int = 0

    protected fun createThreadEvent() {
        if (threadEvent == null) {
            threadEvent = ThreadEvent(this)
        }
    }

    override fun fireThreadRunStarted() {
        val listeners = eventListenerList.listenerList
        createThreadEvent()
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === IThreadListener::class.java) {
                (listeners[i + 1] as IThreadListener).threadRunStarted(threadEvent!!)
            }
        }
    }

    override fun fireThreadRunning() {
        val listeners = eventListenerList.listenerList
        createThreadEvent()
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === IThreadListener::class.java) {
                (listeners[i + 1] as IThreadListener).threadRunning(threadEvent!!)
            }
        }
    }

    override fun fireThreadRunFailed() {
        val listeners = eventListenerList.listenerList
        createThreadEvent()
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === IThreadListener::class.java) {
                (listeners[i + 1] as IThreadListener).threadRunFailed(threadEvent!!)
            }
        }
    }

    override fun fireThreadRunFinished() {
        val listeners = eventListenerList.listenerList
        createThreadEvent()
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === IThreadListener::class.java) {
                (listeners[i + 1] as IThreadListener).threadRunFinished(threadEvent!!)
            }
        }
    }

    override fun addThreadListener(threadListener: IThreadListener) {
        eventListenerList.add(IThreadListener::class.java, threadListener)
    }
}
