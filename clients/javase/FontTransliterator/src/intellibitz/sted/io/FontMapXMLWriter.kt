package intellibitz.sted.io

import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.util.Resources
import org.xml.sax.InputSource

import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult
import java.io.BufferedReader
import java.io.StringReader

class FontMapXMLWriter private constructor() {
    companion object {
        @Throws(TransformerException::class)
        fun write(fontMap: FontMap) {
            val selectedFile = fontMap.fontMapFile ?: return
            // Use a Transformer for output
            val transformer = TransformerFactory.newInstance().newTransformer()
            // Use the parser as a SAX source for input
            val stringBuilder = StringBuilder()
            stringBuilder.append(selectedFile.name)
            stringBuilder.append(Resources.NEWLINE_DELIMITER)
            stringBuilder.append(Resources.getVersion())
            stringBuilder.append(Resources.NEWLINE_DELIMITER)
            stringBuilder.append(fontMap.toString())
            val source = SAXSource(
                FontMapXMLReader(),
                InputSource(BufferedReader(StringReader(stringBuilder.toString())))
            )
            transformer.transform(source, StreamResult(selectedFile))
        }
    }
}
