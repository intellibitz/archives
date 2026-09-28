package intellibitz.sted.widgets

import intellibitz.sted.event.IKeypadListener
import intellibitz.sted.event.KeypadEvent
import intellibitz.sted.ui.FontKeypad
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.JButton
import javax.swing.JTextField
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener

open class FontChangeTextField : JTextField(), ItemListener, ActionListener, TableModelListener, IKeypadListener {
    override fun itemStateChanged(e: ItemEvent) {
        val f = Font(e.item.toString(), Font.PLAIN, 14)
        font = f
    }

    override fun actionPerformed(e: ActionEvent) {
        text = text + (e.source as JButton).text
    }

    override fun tableChanged(e: TableModelEvent) {
        requestFocus()
    }

    override fun keypadReset(event: KeypadEvent) {
        val fontKeypad = event.eventSource as FontKeypad
        val keys = fontKeypad.keys
        val sz = keys.size
        for (i in 0 until sz) {
            keys[i].addActionListener(this)
        }
    }
}
