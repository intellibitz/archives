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
 * $Id:FontMapReader.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/FontMapReader.kt $
 */

package intellibitz.sted.io

import intellibitz.sted.event.FontMapReadEvent
import intellibitz.sted.event.ThreadEventSourceBase
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.util.FileHelper
import intellibitz.sted.util.Resources
import org.xml.sax.SAXException
import java.awt.HeadlessException
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.StringTokenizer
import java.util.logging.Logger
import javax.xml.parsers.ParserConfigurationException

class FontMapReader(private val fontMap: FontMap) : ThreadEventSourceBase() {

    companion object {
        private const val FONT1_TITLE = "FONT1"
        private const val FONT2_TITLE = "FONT2"
        private const val HEADER = "FONT MAPPER 1.0"
        private const val HEADER_TITLE = "HEADER"
        private const val INT_DELIMITER = ":"
        private const val PROPERTY_DELIMITER = "="
        private const val STARTS_WITH = "STARTS"
        private const val ENDS_WITH = "ENDS"
        private const val FOLLOWED_BY = "FBY"
        private const val PRECEDED_BY = "PBY"
        private const val COMMA = ","

        private val logger = Logger.getLogger("intellibitz.sted.io.FontMapReader")

        @Throws(IOException::class, SAXException::class, ParserConfigurationException::class)
        fun read(fontMap: FontMap) {
            val file = fontMap.fontMapFile ?: throw IOException("FontMap file is null")
            if (file.name.lowercase().endsWith(Resources.XML)) {
                FontMapXMLHandler().read(fontMap)
            } else {
                readOldFormat(fontMap)
            }
        }

        @Throws(IOException::class)
        private fun readOldFormat(fontMap: FontMap) {
            val file = fontMap.fontMapFile ?: throw IOException("FontMap file is null")
            val bufferedReader = BufferedReader(InputStreamReader(FileHelper.getInputStream(file)))
            var headerFound = false
            var input: String? = bufferedReader.readLine()
            while (input != null) {
                if (input.isNotEmpty() && !input.startsWith("#") && !input.startsWith("//") && !input.startsWith("/*")) {
                    if (input.indexOf(COMMA) != -1) {
                        createFontMapEntry(input)?.let { fontMap.entries.add(it) }
                    } else {
                        val stringTokenizer = StringTokenizer(input.trim(), PROPERTY_DELIMITER, false)
                        if (stringTokenizer.countTokens() < 2) {
                            throw IOException("$file invalid fontmap - no header found")
                        }
                        val token1 = stringTokenizer.nextToken()
                        val token2 = stringTokenizer.nextToken()
                        if (!headerFound && HEADER_TITLE == token1) {
                            if (HEADER == token2) {
                                headerFound = true
                            }
                        } else if (!headerFound) {
                            throw IOException("$file invalid fontmap - no header found - quitting")
                        } else if (FONT1_TITLE == token1) {
                            fontMap.setFont1(token2)
                        } else if (FONT2_TITLE == token1) {
                            fontMap.setFont2(token2)
                        } else {
                            fontMap.entries.add(FontMapEntry(token1, token2))
                        }
                    }
                }
                input = bufferedReader.readLine()
            }
        }

        private fun createFontMapEntry(value: String): FontMapEntry? {
            var entry: FontMapEntry? = null
            val stringTokenizer = StringTokenizer(value, INT_DELIMITER)
            if (stringTokenizer.countTokens() == 2) {
                entry = FontMapEntry()
                val key = stringTokenizer.nextToken()
                var st = StringTokenizer(key, PROPERTY_DELIMITER)
                if (st.countTokens() == 2) {
                    entry.from = st.nextToken()
                    entry.to = st.nextToken()
                }
                st = StringTokenizer(stringTokenizer.nextToken(), COMMA)
                while (st.hasMoreTokens()) {
                    val st2 = StringTokenizer(st.nextToken(), PROPERTY_DELIMITER)
                    if (st2.countTokens() == 2) {
                        val ruleprop = st2.nextToken()
                        if (STARTS_WITH == ruleprop) {
                            entry.setBeginsWith(st2.nextToken())
                        } else if (ENDS_WITH == ruleprop) {
                            entry.setEndsWith(st2.nextToken())
                        } else if (FOLLOWED_BY == ruleprop) {
                            entry.followedBy = st2.nextToken()
                        } else if (PRECEDED_BY == ruleprop) {
                            entry.precededBy = st2.nextToken()
                        }
                    }
                }
            }
            return entry
        }
    }

    init {
        if (fontMap.fontMapFile == null) {
            throw IllegalArgumentException("Cannot Load - File is null")
        }
        threadEvent = FontMapReadEvent(this)
    }

    override fun run() {
        fireThreadRunStarted()
        try {
            read(fontMap)
            fireThreadRunFinished()
        } catch (e: IOException) {
            message = "Invalid FontMap ${fontMap.fontMapFile?.absolutePath}"
            logger.throwing("intellibitz.sted.actions.LoadFontMapAction", "readFontMap", e)
            fireThreadRunFailed()
        } catch (e: SAXException) {
            message = "Invalid FontMap ${fontMap.fontMapFile?.absolutePath}"
            logger.throwing("intellibitz.sted.actions.LoadFontMapAction", "readFontMap", e)
            fireThreadRunFailed()
        } catch (e: ParserConfigurationException) {
            message = "Invalid FontMap ${fontMap.fontMapFile?.absolutePath}"
            logger.throwing("intellibitz.sted.actions.LoadFontMapAction", "readFontMap", e)
            fireThreadRunFailed()
        } catch (e: HeadlessException) {
            message = "Invalid FontMap ${fontMap.fontMapFile?.absolutePath}"
            logger.throwing("intellibitz.sted.actions.LoadFontMapAction", "readFontMap", e)
            fireThreadRunFailed()
        }
    }
}
