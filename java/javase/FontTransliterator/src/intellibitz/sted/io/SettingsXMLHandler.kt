package intellibitz.sted.io

import intellibitz.sted.util.FileHelper
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.io.IOException
import java.util.HashMap
import java.util.logging.Logger
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory

class SettingsXMLHandler : DefaultHandler {
    private var stedSettingsFile: File? = null
    val settings: MutableMap<String, String> = HashMap()
    private var key: String? = null
    private var value: String? = null

    companion object {
        private val logger = Logger.getLogger("intellibitz.sted.io.SettingsXMLHandler")
    }

    constructor() : super()

    constructor(stedSettings: File) : this() {
        stedSettingsFile = stedSettings
    }

    @Throws(IOException::class, ParserConfigurationException::class, SAXException::class)
    fun read() {
        logger.entering(javaClass.name, "read", stedSettingsFile)
        val saxParserFactory = SAXParserFactory.newInstance()
        saxParserFactory.isValidating = true
        val saxParser = saxParserFactory.newSAXParser()
        logger.info("reading settings file - ${stedSettingsFile?.absolutePath}")
        if (stedSettingsFile == null) throw IOException("InputStream NULL - Cannot Read File: null")
        val inputStream = FileHelper.getInputStream(stedSettingsFile!!)
        if (inputStream == null) {
            logger.severe("InputStream NULL - Cannot Read File: $stedSettingsFile")
            throw IOException("InputStream NULL - Cannot Read File: $stedSettingsFile")
        } else {
            saxParser.parse(inputStream, this)
        }
        logger.exiting(javaClass.name, "read")
    }

    @Throws(SAXException::class)
    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        if ("settings" == qName) {
        } else if ("option" == qName) {
            key = attributes.getValue("name")
            value = attributes.getValue("value")
        }
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
        if ("option" == qName) {
            if (key != null && value != null) {
                settings[key!!] = value!!
            }
        }
    }
}
