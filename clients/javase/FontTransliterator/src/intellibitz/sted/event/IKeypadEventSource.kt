package intellibitz.sted.event

interface IKeypadEventSource {
    fun fireKeypadReset()
    fun addKeypadListener(statusListener: IKeypadListener)
}
