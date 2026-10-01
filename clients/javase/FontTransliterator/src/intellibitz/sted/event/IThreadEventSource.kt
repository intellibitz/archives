package intellibitz.sted.event

interface IThreadEventSource : Runnable {
    fun fireThreadRunStarted()
    fun fireThreadRunning()
    fun fireThreadRunFailed()
    fun fireThreadRunFinished()
    fun addThreadListener(threadListener: IThreadListener)
    
    val message: Any?
    val result: Any?
    val progress: Int
    val progressMaximum: Int
}
