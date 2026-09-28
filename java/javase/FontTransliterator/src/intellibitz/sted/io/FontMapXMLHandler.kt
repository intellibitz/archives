package intellibitz.sted.io

import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.util.FileHelper
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.util.logging.Logger
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory

class FontMapXMLHandler : DefaultHandler {
    private var fontMap: FontMap? = null
    private var fontMapEntry: FontMapEntry? = null
    private var valid = false

    companion object {
        private val logger = Logger.getLogger("intellibitz.sted.io.FontMapXMLHandler")
    }

    constructor() : super()

    constructor(fontMap: FontMap) : this() {
        this.fontMap = fontMap
    }

    @Throws(IOException::class, ParserConfigurationException::class, SAXException::class)
    fun read(fontMap: FontMap) {
        logger.entering(javaClass.name, "read", fontMap)
        this.fontMap = fontMap
        val saxParserFactory = SAXParserFactory.newInstance()
        saxParserFactory.isValidating = true
        val saxParser = saxParserFactory.newSAXParser()
        logger.info("parsing file - ${fontMap.fontMapFile}")
        val inputStream = FileHelper.getInputStream(fontMap.fontMapFile!!)
        if (inputStream == null) {
            logger.severe("InputStream NULL - Cannot Read File: ${fontMap.fontMapFile}")
            throw IOException("InputStream NULL - Cannot Read File: ${fontMap.fontMapFile}")
        } else {
            saxParser.parse(inputStream, this)
        }
        logger.exiting(javaClass.name, "read")
    }

    @Throws(SAXException::class)
    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        if ("fontmap" == qName) {
            valid = true
        } else if ("font_from" == qName) {
            fontMap?.font1Path = attributes.getValue("path")
            fontMap?.setFont1(attributes.getValue("value"))
        } else if ("font_to" == qName) {
            fontMap?.font2Path = attributes.getValue("path")
            fontMap?.setFont2(attributes.getValue("value"))
        } else if ("font_entry" == qName) {
            fontMapEntry = FontMapEntry()
        } else if ("entry_from" == qName) {
            fontMapEntry?.from = attributes.getValue("value") ?: ""
        } else if ("entry_to" == qName) {
            fontMapEntry?.to = attributes.getValue("value") ?: ""
        } else if ("begins_with" == qName) {
            fontMapEntry?.setBeginsWith(attributes.getValue("value"))
        } else if ("ends_with" == qName) {
            fontMapEntry?.setEndsWith(attributes.getValue("value"))
        } else if ("preceded_by" == qName) {
            fontMapEntry?.precededBy = attributes.getValue("value")
        } else if ("followed_by" == qName) {
            fontMapEntry?.followedBy = attributes.getValue("value")
        } else if ("conditional" == qName) {
            fontMapEntry?.conditional = attributes.getValue("value")
        }
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
        if (!valid) {
            throw SAXException("Invalid FontMap.. Did not have a valid Header")
        }
        if ("font_entry" == qName) {
            fontMapEntry?.let { fontMap?.entries?.add(it) }
        }
    }
}
