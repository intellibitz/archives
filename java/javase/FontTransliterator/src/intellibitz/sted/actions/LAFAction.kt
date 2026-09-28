package intellibitz.sted.actions

import intellibitz.sted.launch.STEDGUI
import intellibitz.sted.ui.AboutSTED
import intellibitz.sted.ui.HelpWindow
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import java.awt.Component
import java.awt.event.ActionEvent
import java.util.ArrayList
import java.util.logging.Logger
import javax.swing.UnsupportedLookAndFeelException

class LAFAction : STEDWindowAction() {
    companion object {
        private val myLogger = Logger.getLogger("intellibitz.sted.actions.LAFAction")
    }

    override fun actionPerformed(e: ActionEvent) {
        try {
            val collection = ArrayList<Component>()
            val stedWindow = getSTEDWindow()
            collection.add(stedWindow)
            val help = HelpWindow.instance!!
            collection.add(help)
            val aboutDialog = AboutSTED.instance!!
            collection.add(aboutDialog)
            val component = MenuHandler.instance!!.getPopupMenu(Resources.MENU_POPUP_MAPPING)
            if (component != null) {
                collection.add(component)
            }
            STEDGUI.updateUIWithLAF(e.actionCommand, collection.iterator())
        } catch (e1: ClassNotFoundException) {
            myLogger.throwing(javaClass.name, "actionPerformed", e1)
            fireMessagePosted("Unable to Set LookAndFeel - Class Not Found: " + e1.message)
            e1.printStackTrace()
        } catch (e1: InstantiationException) {
            myLogger.throwing(javaClass.name, "actionPerformed", e1)
            fireMessagePosted("Unable to Set LookAndFeel - Cannot Instantiate: " + e1.message)
            e1.printStackTrace()
        } catch (e1: IllegalAccessException) {
            myLogger.throwing(javaClass.name, "actionPerformed", e1)
            fireMessagePosted("Unable to Set LookAndFeel - IllegalAccess: " + e1.message)
            e1.printStackTrace()
        } catch (e1: UnsupportedLookAndFeelException) {
            myLogger.throwing(javaClass.name, "actionPerformed", e1)
            fireMessagePosted("Unable to Set LookAndFeel - Unsupported: " + e1.message)
            e1.printStackTrace()
        }
    }
}
