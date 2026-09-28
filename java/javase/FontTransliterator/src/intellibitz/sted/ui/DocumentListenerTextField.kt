package intellibitz.sted.ui

import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.SampleTextConverter
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import intellibitz.sted.widgets.FontChangeTextField
import javax.swing.JCheckBoxMenuItem
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class DocumentListenerTextField : FontChangeTextField(), DocumentListener {
    private var converter: SampleTextConverter? = null
    private var fontMap: FontMap? = null
    private var mapperPanel: MapperPanel? = null

    fun load() {}

    override fun insertUpdate(e: DocumentEvent) {
        convertSampleText(e)
    }

    override fun removeUpdate(e: DocumentEvent) {
        convertSampleText(e)
    }

    override fun changedUpdate(e: DocumentEvent) {
        convertSampleText(e)
    }

    private fun convertSampleText(e: DocumentEvent) {
        if (e.document.length > 0) {
            if (converter == null) {
                converter = SampleTextConverter(mapperPanel!!)
            }
            converter!!.setFontMap(fontMap)
            val menuHandler = MenuHandler.instance!!
            val preserve = menuHandler.getMenuItem(Resources.ACTION_PRESERVE_TAGS) as JCheckBoxMenuItem?
            converter!!.setHTMLAware(preserve?.isSelected ?: false)
            val reverse = menuHandler.getMenuItem(Resources.ACTION_TRANSLITERATE_REVERSE) as JCheckBoxMenuItem?
            converter!!.setReverseTransliterate(reverse?.isSelected ?: false)
            SwingUtilities.invokeLater(converter)
        }
    }

    fun setFontMap(fontMap: FontMap?) {
        this.fontMap = fontMap
    }

    fun setFontMapperPanel(mapperPanel: MapperPanel?) {
        this.mapperPanel = mapperPanel
    }
}
