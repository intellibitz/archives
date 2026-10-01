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
 * $Id:SettingsXMLWriter.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/SettingsXMLWriter.kt $
 */

package intellibitz.sted.io

import intellibitz.sted.ui.STEDWindow
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.DTDHandler
import org.xml.sax.EntityResolver
import org.xml.sax.ErrorHandler
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.SAXNotRecognizedException
import org.xml.sax.SAXNotSupportedException
import org.xml.sax.XMLReader
import org.xml.sax.helpers.AttributesImpl
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.StringReader
import java.util.StringTokenizer

class SettingsXMLWriter {
    private constructor()
    class SettingsXMLReader : XMLReader {
    private var contentHandler: ContentHandler? = null
    private val nsu: String = Resources.EMPTY_STRING
    private val indent: String = "\n"
    @Throws(IOException::class, SAXException::class)
    fun parse(input: InputSource) {
        if ((contentHandler == null)) {
            throw SAXException("No content contentHandler")
        }
        val rootElement: String = "settings"
        val bufferedReader: BufferedReader = BufferedReader(input.getCharacterStream())
        contentHandler.startDocument()
        val atts: AttributesImpl = AttributesImpl()
        atts.addAttribute(nsu, Resources.EMPTY_STRING, "name", "ID", bufferedReader.readLine())
        atts.addAttribute(nsu, Resources.EMPTY_STRING, "version", "CDATA", bufferedReader.readLine())
        contentHandler.startElement(nsu, rootElement, rootElement, atts)
        newLine()
        var line: String
        while ((line = bufferedReader.readLine() != null)) {
            writeOption(line)
            newLine()
        }
        contentHandler.endElement(nsu, rootElement, rootElement)
        newLine()
        contentHandler.endDocument()
    }
    @Throws(SAXException::class)
    private fun writeElement(uri: String, localName: String, qName: String, atts: Attributes) {
        contentHandler.startElement(uri, localName, qName, atts)
        contentHandler.endElement(uri, localName, qName)
    }
    @Throws(SAXException::class)
    private fun newLine() {
        contentHandler.ignorableWhitespace(indent.toCharArray(), 0, indent.length())
    }
    @Throws(SAXException::class)
    private fun writeOption(entry: String) {
        val stringTokenizer: StringTokenizer = StringTokenizer(entry, Resources.SYMBOL_ASTERISK)
        while (stringTokenizer.hasMoreElements()) {
            val atts: AttributesImpl = AttributesImpl()
            val name: String = stringTokenizer.nextToken()
            val value: String = stringTokenizer.nextToken()
            atts.addAttribute(nsu, Resources.EMPTY_STRING, "name", "ID", name)
            atts.addAttribute(nsu, Resources.EMPTY_STRING, "value", "CDATA", value)
            writeElement(nsu, "option", "option", atts)
        }
    }
    fun getContentHandler(): ContentHandler {
        return contentHandler
    }
    fun setContentHandler(handler: ContentHandler) {
        contentHandler = handler
    }
    @Throws(IOException::class, SAXException::class)
    fun parse(systemId: String) {

    }
    @Throws(SAXNotRecognizedException::class, SAXNotSupportedException::class)
    fun getFeature(name: String): Boolean {
        return false
    }
    @Throws(SAXNotRecognizedException::class, SAXNotSupportedException::class)
    fun setFeature(name: String, value: Boolean) {

    }
    fun getDTDHandler(): DTDHandler {
        return null
    }
    fun setDTDHandler(handler: DTDHandler) {

    }
    fun getEntityResolver(): EntityResolver {
        return null
    }
    fun setEntityResolver(resolver: EntityResolver) {

    }
    fun getErrorHandler(): ErrorHandler {
        return null
    }
    fun setErrorHandler(handler: ErrorHandler) {

    }
    @Throws(SAXNotRecognizedException::class, SAXNotSupportedException::class)
    fun getProperty(name: String): Object {
        return null
    }
    @Throws(SAXNotRecognizedException::class, SAXNotSupportedException::class)
    fun setProperty(name: String, value: Object) {

    }
    }

    companion object {
        @Throws(TransformerException::class)
        @JvmStatic
        fun writeUserSettings(stedWindow: STEDWindow) {
            val file: File = File(Resources.SETTINGS_FILE_PATH_USER)
            val stringBuffer: StringBuffer = StringBuffer()
            stringBuffer.append(file.getName())
            stringBuffer.append(Resources.NEWLINE_DELIMITER)
            stringBuffer.append(Resources.getVersion())
            stringBuffer.append(Resources.NEWLINE_DELIMITER)
            stringBuffer.append(MenuHandler.getUserOptions())
            write(file, stringBuffer.toString())
        }
        @Throws(TransformerException::class)
        private @JvmStatic
        fun write(file: File, fileContents: String) {
            val transformer: Transformer = TransformerFactory.newInstance()
            val source: SAXSource = SAXSource(SettingsXMLReader(), InputSource(BufferedReader(StringReader(fileContents))))
            transformer.transform(source, StreamResult(file))
        }
    }
}
