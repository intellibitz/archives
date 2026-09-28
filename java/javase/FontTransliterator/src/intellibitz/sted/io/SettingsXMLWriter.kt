package intellibitz.sted.io

import intellibitz.sted.ui.STEDWindow
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import org.xml.sax.*
import org.xml.sax.helpers.AttributesImpl
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.StringReader
import java.util.StringTokenizer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult

class SettingsXMLWriter private constructor() {
    companion object {
        @Throws(TransformerException::class)
        fun writeUserSettings(stedWindow: STEDWindow?) {
            val file = File(Resources.SETTINGS_FILE_PATH_USER)
            // Use the parser as a SAX source for input
            val stringBuffer = StringBuffer()
            stringBuffer.append(file.name)
            stringBuffer.append(Resources.NEWLINE_DELIMITER)
            stringBuffer.append(Resources.getVersion())
            stringBuffer.append(Resources.NEWLINE_DELIMITER)
            // write all the options that were customizable by the user
            stringBuffer.append(MenuHandler.getUserOptions())
            write(file, stringBuffer.toString())
        }

        @Throws(TransformerException::class)
        private fun write(file: File, fileContents: String) {
            // Use a Transformer for output
            val transformer = TransformerFactory.newInstance().newTransformer()
            val source = SAXSource(
                SettingsXMLReader(),
                InputSource(BufferedReader(StringReader(fileContents)))
            )
            transformer.transform(source, StreamResult(file))
        }
    }

    internal class SettingsXMLReader : XMLReader {
        private var contentHandler: ContentHandler? = null
        // We're not doing namespaces, and we have no    // attributes on our elements.
        private val nsu = Resources.EMPTY_STRING // NamespaceURI
        private val indent = "\n" // for readability

        @Throws(IOException::class, SAXException::class)
        override fun parse(input: InputSource) {
            if (contentHandler == null) {
                throw SAXException("No content contentHandler")
            }
            val rootElement = "settings"
            // Get an efficient reader for the file
            val bufferedReader = BufferedReader(input.characterStream)
            // Read the file and display it's contents.
            contentHandler!!.startDocument()
            val atts = AttributesImpl()
            atts.addAttribute(nsu, Resources.EMPTY_STRING, "name", "ID", bufferedReader.readLine())
            atts.addAttribute(nsu, Resources.EMPTY_STRING, "version", "CDATA", bufferedReader.readLine())
            contentHandler!!.startElement(nsu, rootElement, rootElement, atts)
            newLine()
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                writeOption(line)
                newLine()
                line = bufferedReader.readLine()
            }
            contentHandler!!.endElement(nsu, rootElement, rootElement)
            newLine()
            contentHandler!!.endDocument()
        }

        @Throws(SAXException::class)
        private fun writeElement(uri: String, localName: String, qName: String, atts: Attributes) {
            contentHandler!!.startElement(uri, localName, qName, atts)
            contentHandler!!.endElement(uri, localName, qName)
        }

        @Throws(SAXException::class)
        private fun newLine() {
            contentHandler!!.ignorableWhitespace(indent.toCharArray(), 0, indent.length)
        }

        @Throws(SAXException::class)
        private fun writeOption(entry: String) {
            val stringTokenizer = StringTokenizer(entry, Resources.SYMBOL_ASTERISK)
            while (stringTokenizer.hasMoreElements()) {
                val atts = AttributesImpl()
                val name = stringTokenizer.nextToken()
                val value = stringTokenizer.nextToken()
                atts.addAttribute(nsu, Resources.EMPTY_STRING, "name", "ID", name)
                atts.addAttribute(nsu, Resources.EMPTY_STRING, "value", "CDATA", value)
                writeElement(nsu, "option", "option", atts)
            }
        }

        override fun getContentHandler(): ContentHandler? {
            return contentHandler
        }

        override fun setContentHandler(handler: ContentHandler) {
            contentHandler = handler
        }

        @Throws(IOException::class, SAXException::class)
        override fun parse(systemId: String) {
        }

        @Throws(SAXNotRecognizedException::class, SAXNotSupportedException::class)
        override fun getFeature(name: String): Boolean {
            return false
        }

        @Throws(SAXNotRecognizedException::class, SAXNotSupportedException::class)
        override fun setFeature(name: String, value: Boolean) {
        }

        override fun getDTDHandler(): DTDHandler? {
            return null
        }

        override fun setDTDHandler(handler: DTDHandler) {
        }

        override fun getEntityResolver(): EntityResolver? {
            return null
        }

        override fun setEntityResolver(resolver: EntityResolver) {
        }

        override fun getErrorHandler(): ErrorHandler? {
            return null
        }

        override fun setErrorHandler(handler: ErrorHandler) {
        }

        @Throws(SAXNotRecognizedException::class, SAXNotSupportedException::class)
        override fun getProperty(name: String): Any? {
            return null
        }

        @Throws(SAXNotRecognizedException::class, SAXNotSupportedException::class)
        override fun setProperty(name: String, value: Any) {
        }
    }
}
