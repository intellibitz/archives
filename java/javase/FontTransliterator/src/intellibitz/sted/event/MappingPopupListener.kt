package intellibitz.sted.event

import intellibitz.sted.actions.TableRowsSelectAction
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPopupMenu
import javax.swing.JTable

class MappingPopupListener : MouseAdapter() {
    private var popupMenu: JPopupMenu? = null

    fun load() {
        popupMenu = MenuHandler.instance!!.getPopupMenu(Resources.MENU_POPUP_MAPPING)
    }

    override fun mousePressed(e: MouseEvent) {
        maybeShowPopup(e)
    }

    override fun mouseReleased(e: MouseEvent) {
        maybeShowPopup(e)
    }

    private fun maybeShowPopup(e: MouseEvent) {
        if (e.isPopupTrigger) {
            setTableOnAction(e.source as JTable)
            popupMenu?.show(e.component, e.x, e.y)
        }
    }

    private fun setTableOnAction(table: JTable) {
        val actions = MenuHandler.actions
        for (action in actions.values) {
            if (action is TableRowsSelectAction) {
                action.table = table
            }
        }
    }
}
