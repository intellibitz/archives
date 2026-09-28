package intellibitz.sted.actions

import intellibitz.sted.io.SettingsXMLWriter
import intellibitz.sted.ui.STEDWindow
import java.awt.event.ActionEvent
import java.awt.event.WindowEvent
import javax.swing.JOptionPane
import javax.xml.transform.TransformerException

class ExitAction : STEDWindowAction() {
    override fun actionPerformed(e: ActionEvent) {
        exit()
    }

    private fun exit() {
        val stedWindow = getSTEDWindow()
        if (JOptionPane.CANCEL_OPTION != stedWindow.desktop!!.saveDirty()) {
            try {
                SettingsXMLWriter.writeUserSettings(stedWindow)
            } catch (ex: TransformerException) {
                logger.severe("Unable to write User Settings - TransformerException " + ex.message)
                logger.throwing("ExitAction", "exit", ex)
            }
            Runtime.getRuntime().gc()
            System.runFinalization()
            System.exit(0)
        }
    }

    override fun windowClosing(e: WindowEvent) {
        exit()
    }
}
