package intellibitz.sted.actions

import intellibitz.sted.ui.STEDWindow
import intellibitz.sted.util.MenuHandler
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.AbstractButton
import javax.swing.Action

open class ItemListenerAction : STEDWindowAction(), ItemListener {
    override fun actionPerformed(e: ActionEvent) {}

    override fun itemStateChanged(e: ItemEvent) {
        var state = true
        if (ItemEvent.DESELECTED == e.stateChange) {
            state = false
        }
        val obj = e.source
        val action: Action
        if (obj is AbstractButton) {
            val stedWindow = getSTEDWindow()
            action = obj.action
            var button: AbstractButton? = MenuHandler.instance!!.getMenuItem(action.getValue(Action.NAME) as String)
            if (button != null) {
                button.isSelected = state
            }
            button = MenuHandler.instance!!.getToolButton(action.getValue(Action.NAME) as String)
            if (button != null) {
                button.isSelected = state
            }
        }
    }
}
