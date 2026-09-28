package intellibitz.sted.event

interface IMessageEventSource {
    fun fireMessagePosted()
    fun addMessageListener(messageListener: IMessageListener)
}
