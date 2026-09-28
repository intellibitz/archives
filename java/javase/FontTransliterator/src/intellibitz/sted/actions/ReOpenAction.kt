package intellibitz.sted.actions

import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import java.awt.event.ActionEvent
import javax.swing.event.InternalFrameEvent
import javax.swing.event.InternalFrameListener

class ReOpenAction : ReOpenFontMapAction(), FontMapChangeListener, InternalFrameListener {

    override fun actionPerformed(e: ActionEvent) {}

    override fun stateChanged(e: FontMapChangeEvent) {
        val fontMap = e.fontMap
        if (fontMap!!.isNew()) {
            MenuHandler.enableReOpenItems(MenuHandler.instance!!)
        } else {
            val fileName = fontMap.getFileName()
            val stedWindow = getSTEDWindow()
            stedWindow.desktop!!.addItemToReOpenMenu(fileName)
            val menu = MenuHandler.instance!!.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND)
            MenuHandler.disableMenuItem(menu!!, fileName)
        }
    }

    override fun internalFrameActivated(e: InternalFrameEvent) {
        MenuHandler.enableItemsInReOpenMenu(MenuHandler.instance!!, getSTEDWindow().desktop!!.fontMap!!)
    }

    override fun internalFrameOpened(e: InternalFrameEvent) {
        MenuHandler.enableItemsInReOpenMenu(MenuHandler.instance!!, getSTEDWindow().desktop!!.fontMap!!)
    }

    override fun internalFrameDeactivated(e: InternalFrameEvent) {
        addItemToReOpenMenu()
    }

    override fun internalFrameClosing(e: InternalFrameEvent) {
        addItemToReOpenMenu()
    }

    override fun internalFrameClosed(e: InternalFrameEvent) {}
    override fun internalFrameDeiconified(e: InternalFrameEvent) {}
    override fun internalFrameIconified(e: InternalFrameEvent) {}

    fun addItemToReOpenMenu() {
        val stedWindow = getSTEDWindow()
        val menuHandler = MenuHandler.instance!!
        val fontMap = stedWindow.desktop!!.fontMap!!
        val menu = menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND)
        if (!fontMap.isNew()) {
            MenuHandler.addReOpenItem(menu!!, fontMap.getFileName())
        }
        MenuHandler.enableReOpenItems(menu!!)
    }
}
