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
 * $Id:FontMapXMLReader.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/FontMapXMLReader.kt $
 */

package intellibitz.sted.io

import intellibitz.sted.util.Resources
import org.xml.sax.*
import org.xml.sax.helpers.AttributesImpl
import java.io.BufferedReader
import java.io.IOException
import java.util.StringTokenizer

internal class FontMapXMLReader : XMLReader {
    private var contentHandler: ContentHandler? = null

    // We're not doing namespaces, and we have no
    // attributes on our elements.
    private val nsu = Resources.EMPTY_STRING  // NamespaceURI
    private val indent = "\n" // for readability

    companion object {
        private val EMPTY_ATTRIBUTES: Attributes = AttributesImpl()

        private fun getAttributeImpl(uri: String, localName: String,
                                     qName: String, type: String,
                                     value: String): Attributes {
            val atts = AttributesImpl()
            atts.addAttribute(uri, localName, qName, type, value)
            return atts
        }
    }

    @Throws(IOException::class, SAXException::class)
    override fun parse(input: InputSource) {
        if (contentHandler == null) {
            throw SAXException("No content contentHandler")
        }
        val rootElement = "fontmap"
        // Get an efficient reader for the file
        val bufferedReader = BufferedReader(input.characterStream)
        // Read the file and display it's contents.
        contentHandler!!.startDocument()
        val atts = AttributesImpl()
        atts.addAttribute(nsu, Resources.EMPTY_STRING, "name", "ID", bufferedReader.readLine())
        atts.addAttribute(nsu, Resources.EMPTY_STRING, "version", "CDATA", bufferedReader.readLine())
        contentHandler!!.startElement(nsu, rootElement, rootElement, atts)
        newLine()
        contentHandler!!.startElement(nsu, "font", "font", EMPTY_ATTRIBUTES)
        newLine()
        writeElement(nsu, "font_from", "font_from", getFontAttributes(bufferedReader.readLine()))
        newLine()
        writeElement(nsu, "font_to", "font_to", getFontAttributes(bufferedReader.readLine()))
        newLine()
        contentHandler!!.endElement(nsu, "font", "font")
        newLine()
        var line: String? = bufferedReader.readLine()
        while (line != null) {
            writeFontEntry(line)
            newLine()
            line = bufferedReader.readLine()
        }
        contentHandler!!.endElement(nsu, rootElement, rootElement)
        newLine()
        contentHandler!!.endDocument()
    }

    private fun getFontAttributes(line: String): Attributes {
        val atts = AttributesImpl()
        val stringTokenizer = StringTokenizer(line, Resources.SYMBOL_ASTERISK)
        atts.addAttribute(nsu, Resources.EMPTY_STRING, "value", "CDATA", stringTokenizer.nextToken())
        atts.addAttribute(nsu, Resources.EMPTY_STRING, "path", "CDATA", stringTokenizer.nextToken())
        return atts
    }

    private fun getValueAttribute(valStr: String): Attributes {
        return getAttributeImpl(nsu, Resources.EMPTY_STRING, "value", "CDATA", valStr)
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
    private fun writeFontEntry(entry: String) {
        contentHandler!!.startElement(nsu, "font_entry", "font_entry", EMPTY_ATTRIBUTES)
        newLine()
        val stringTokenizer = StringTokenizer(entry, Resources.ENTRY_TOSTRING_DELIMITER)
        var i = 0
        while (stringTokenizer.hasMoreElements()) {
            val token = stringTokenizer.nextToken()
            if (!(token.lowercase().startsWith("null")
                        || token.lowercase().startsWith("false")
                        || Resources.EMPTY_STRING == token)) {
                when (i) {
                    0 -> {
                        writeElement(nsu, "entry_from", "entry_from", getValueAttribute(token))
                        newLine()
                    }
                    1 -> {
                        writeElement(nsu, "entry_to", "entry_to", getValueAttribute(token))
                        newLine()
                    }
                    2 -> {
                        writeElement(nsu, "begins_with", "begins_with", getValueAttribute(token))
                        newLine()
                    }
                    3 -> {
                        writeElement(nsu, "ends_with", "ends_with", getValueAttribute(token))
                        newLine()
                    }
                    4 -> {
                        writeElement(nsu, "followed_by", "followed_by", getValueAttribute(token))
                        newLine()
                    }
                    5 -> {
                        writeElement(nsu, "preceded_by", "preceded_by", getValueAttribute(token))
                        newLine()
                    }
                    6 -> {
                        writeElement(nsu, "conditional", "conditional", getValueAttribute(token))
                        newLine()
                    }
                }
            }
            i++
        }
        contentHandler!!.endElement(nsu, "font_entry", "font_entry")
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
