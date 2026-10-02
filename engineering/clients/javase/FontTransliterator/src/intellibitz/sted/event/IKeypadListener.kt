package intellibitz.sted.event

import java.util.EventListener

interface IKeypadListener : EventListener {
    fun keypadReset(event: KeypadEvent)
}
