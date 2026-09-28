package intellibitz.sted.actions

import java.awt.event.ActionEvent

class SelectAllAction : TableModelListenerAction() {
    override fun actionPerformed(e: ActionEvent) {
        super.selectAll()
        fireStatusPosted("Selected All")
    }
}
