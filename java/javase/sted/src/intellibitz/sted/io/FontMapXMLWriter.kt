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
 * $Id:FontMapXMLWriter.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/FontMapXMLWriter.kt $
 */

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
