package intellibitz.sted.actions

import intellibitz.sted.fontmap.Converter
import intellibitz.sted.ui.TabDesktop
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import java.awt.event.ActionEvent
import javax.swing.JCheckBoxMenuItem
import javax.swing.event.TableModelEvent
import javax.swing.table.TableModel

class TransliterateAction : TableModelListenerAction() {
    
    override fun tableChanged(e: TableModelEvent) {
        isEnabled = (e.source as TableModel).rowCount > 0 && getSTEDWindow().desktop!!.fontMapperDesktopFrame!!.enableConverterIfFilesLoaded()
    }

    override fun actionPerformed(e: ActionEvent) {
        val stedWindow = getSTEDWindow()
        val fileToConvert = stedWindow.desktop!!.desktopModel!!.inputFile
        val convertedFile = stedWindow.desktop!!.desktopModel!!.outputFile
        
        if (fileToConvert == null || convertedFile == null) {
            fireMessagePosted("Select valid files for both input and output")
            return
        }
        
        if (fileToConvert.name == convertedFile.name) {
            fireMessagePosted("Input and Output files are same.. select different files")
            return
        }
        
        fireStatusPosted("Begin Converting...")
        val converter = getConverter(stedWindow.desktop!!)
        converter.start()
        isEnabled = false
    }

    fun getConverter(desktop: TabDesktop): Converter {
        val converter = Converter(desktop.fontMap!!, desktop.desktopModel!!.inputFile!!, desktop.desktopModel!!.outputFile!!)
        val preserve = MenuHandler.instance!!.getMenuItem(Resources.ACTION_PRESERVE_TAGS) as JCheckBoxMenuItem
        converter.setHTMLAware(preserve.isSelected)
        val reverse = MenuHandler.instance!!.getMenuItem(Resources.ACTION_TRANSLITERATE_REVERSE) as JCheckBoxMenuItem
        converter.setReverseTransliterate(reverse.isSelected)
        converter.addThreadListener(desktop)
        
        val stop = MenuHandler.instance!!.getAction(Resources.ACTION_STOP_NAME) as TransliterateStopAction
        stop.setConverter(converter)
        stop.isEnabled = true
        return converter
    }
}
