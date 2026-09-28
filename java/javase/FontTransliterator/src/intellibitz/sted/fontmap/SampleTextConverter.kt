package intellibitz.sted.fontmap

import intellibitz.sted.ui.MapperPanel
import intellibitz.sted.util.Resources

class SampleTextConverter(private val mapperPanel: MapperPanel) : Converter() {
    override fun run() {
        val input = mapperPanel.inputText.text
        mapperPanel.outputText.text = Resources.EMPTY_STRING
        if (isReady && !input.isNullOrEmpty()) {
            mapperPanel.outputText.text = transliterate?.parseLine(input)
        }
    }
}
