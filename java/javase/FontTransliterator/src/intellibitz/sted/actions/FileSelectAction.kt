package intellibitz.sted.actions

import intellibitz.sted.launch.STEDGUI
import intellibitz.sted.ui.STEDWindow
import intellibitz.sted.util.Resources
import java.awt.event.ActionEvent
import java.io.File
import javax.swing.JFileChooser

class FileSelectAction : TableModelListenerAction() {
    override fun actionPerformed(e: ActionEvent) {
        val cmd = e.actionCommand
        var isInput = true
        if (Resources.ACTION_SELECT_INPUT_FILE_COMMAND.equals(cmd, ignoreCase = true)) {
            isInput = true
        } else if (Resources.ACTION_SELECT_OUTPUT_FILE_COMMAND.equals(cmd, ignoreCase = true)) {
            isInput = false
        }
        var file: File?
        val stedWindow = getSTEDWindow()
        file = if (isInput) {
            stedWindow.desktop!!.fontMapperDesktopFrame!!.desktopModel!!.inputFile
        } else {
            stedWindow.desktop!!.fontMapperDesktopFrame!!.desktopModel!!.outputFile
        }
        val jFileChooser: JFileChooser
        val result: Int
        if (file == null) {
            jFileChooser = JFileChooser(System.getProperty("user.dir"))
            result = jFileChooser.showOpenDialog(stedWindow)
        } else {
            jFileChooser = JFileChooser(file.parent)
            result = jFileChooser.showSaveDialog(stedWindow)
        }
        if (result == JFileChooser.APPROVE_OPTION) {
            file = jFileChooser.selectedFile
            if (file != null) {
                STEDGUI.busy()
                if (isInput) {
                    stedWindow.desktop!!.fontMapperDesktopFrame!!.desktopModel!!.inputFile = file
                } else {
                    stedWindow.desktop!!.fontMapperDesktopFrame!!.desktopModel!!.outputFile = file
                }
                stedWindow.desktop!!.fontMapperDesktopFrame!!.enableConverterIfFilesLoaded()
                STEDGUI.relax()
            }
        }
    }
}
