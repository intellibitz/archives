/**
 * Copyright (C) IntelliBitz Technologies.,  Muthu Ramadoss
 * 168, Medavakkam Main Road, Madipakkam, Chennai 600091, Tamilnadu, India.
 * http://www.intellibitz.com
 * training@intellibitz.com
 * +91 44 2247 5106
 * http://groups.google.com/group/etoe
 * http://sted.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * STED, Copyright (C) 2007 IntelliBitz Technologies
 * STED comes with ABSOLUTELY NO WARRANTY;
 * This is free software, and you are welcome
 * to redistribute it under the GNU GPL conditions;
 *
 * Visit http://www.gnu.org/ for GPL License terms.
 */

/**
 * $Id:FontMapXMLHandler.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/FontMapXMLHandler.kt $
 */

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
