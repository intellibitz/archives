package intellibitz.sted.widgets

import javax.swing.JButton
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class DocumentListenerButton : JButton(), DocumentListener {
    override fun insertUpdate(e: DocumentEvent) {
        toggle(e)
    }

    override fun removeUpdate(e: DocumentEvent) {
        toggle(e)
    }

    override fun changedUpdate(e: DocumentEvent) {
        toggle(e)
    }

    private fun toggle(e: DocumentEvent) {
        isEnabled = e.document.length > 0
    }
}
