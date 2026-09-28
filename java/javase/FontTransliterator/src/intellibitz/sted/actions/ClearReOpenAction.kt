package intellibitz.sted.actions

import intellibitz.sted.util.MenuHandler
import java.awt.event.ActionEvent

class ClearReOpenAction : STEDWindowAction() {
    override fun actionPerformed(e: ActionEvent) {
        MenuHandler.clearReOpenItems(MenuHandler.instance!!)
    }
}
