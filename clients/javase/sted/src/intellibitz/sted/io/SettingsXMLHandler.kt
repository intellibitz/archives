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
 * $Id:SettingsXMLHandler.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/SettingsXMLHandler.kt $
 */

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
