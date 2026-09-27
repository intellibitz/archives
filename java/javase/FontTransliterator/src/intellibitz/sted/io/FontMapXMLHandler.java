/**
 * Copyright (C) IntelliBitz Technologies.,  Muthu Ramadoss
 * 168, Medavakkam Main Road, Madipakkam, Chennai 600091, Tamilnadu, India.
 * http://www.intellibitz.com
 * training@intellibitz.com
 * +91 44 2247 5106
 * http://groups.google.com/group/etoe
 * http://sted.sourceforge.net
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * <p>
 * STED, Copyright (C) 2007 IntelliBitz Technologies
 * STED comes with ABSOLUTELY NO WARRANTY;
 * This is free software, and you are welcome
 * to redistribute it under the GNU GPL conditions;
 * <p>
 * Visit http://www.gnu.org/ for GPL License terms.
 * <p>
 * $Id:FontMapXMLHandler.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/FontMapXMLHandler.java $
 */

/**
 * $Id:FontMapXMLHandler.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/FontMapXMLHandler.java $
 */

package intellibitz.sted.io;

import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.fontmap.FontMapEntry;
import intellibitz.sted.util.FileHelper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

class FontMapXMLHandler
        extends DefaultHandler {
    private FontMap fontMap;
    private FontMapEntry fontMapEntry;
    private static final Logger logger =
            Logger.getLogger("intellibitz.sted.io.FontMapXMLHandler");
    private boolean valid;

    FontMapXMLHandler() {
        super();
    }

    FontMapXMLHandler(FontMap fontMap) {
        this();
        this.fontMap = fontMap;
    }

    public void read(FontMap fontMap)
            throws IOException, ParserConfigurationException, SAXException {
        logger.entering(getClass().getName(), "read", fontMap);
        this.fontMap = fontMap;
        final SAXParserFactory saxParserFactory =
                SAXParserFactory.newInstance();
        saxParserFactory.setValidating(true);
        final SAXParser saxParser = saxParserFactory.newSAXParser();
        logger.info("parsing file - " + fontMap.getFontMapFile());
        final InputStream inputStream =
                FileHelper.getInputStream(fontMap.getFontMapFile());
        if (inputStream == null) {
            logger.severe("InputStream NULL - Cannot Read File: " +
                    fontMap.getFontMapFile());
            throw new IOException("InputStream NULL - Cannot Read File: " +
                    fontMap.getFontMapFile());
        } else {
            saxParser.parse(inputStream, this);
        }
        logger.exiting(getClass().getName(), "read");
    }

    public void startElement(String uri, String localName, String qName,
                             Attributes attributes)
            throws SAXException {
        if ("fontmap".equals(qName)) {
            valid = true;
        } else if ("font_from".equals(qName)) {
            fontMap.setFont1Path(attributes.getValue("path"));
            fontMap.setFont1(attributes.getValue("value"));
        } else if ("font_to".equals(qName)) {
            fontMap.setFont2Path(attributes.getValue("path"));
            fontMap.setFont2(attributes.getValue("value"));
        } else if ("font_entry".equals(qName)) {
            fontMapEntry = new FontMapEntry();
        } else if ("entry_from".equals(qName)) {
            fontMapEntry.setFrom(attributes.getValue("value"));
        } else if ("entry_to".equals(qName)) {
            fontMapEntry.setTo(attributes.getValue("value"));
        } else if ("begins_with".equals(qName)) {
            fontMapEntry.setBeginsWith(attributes.getValue("value"));
        } else if ("ends_with".equals(qName)) {
            fontMapEntry.setEndsWith(attributes.getValue("value"));
        } else if ("preceded_by".equals(qName)) {
            fontMapEntry.setPrecededBy(attributes.getValue("value"));
        } else if ("followed_by".equals(qName)) {
            fontMapEntry.setFollowedBy(attributes.getValue("value"));
        } else if ("conditional".equals(qName)) {
            fontMapEntry.setConditional(attributes.getValue("value"));
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (!valid) {
            throw new SAXException(
                    "Invalid FontMap.. Did not have a valid Header");
        }
        if ("font_entry".equals(qName)) {
            fontMap.getEntries().add(fontMapEntry);
        }
    }


}
