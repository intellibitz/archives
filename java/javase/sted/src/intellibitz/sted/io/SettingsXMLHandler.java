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
 * $Id:SettingsXMLHandler.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/SettingsXMLHandler.java $
 */

package intellibitz.sted.io;

import intellibitz.sted.util.FileHelper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Reads the Settings XML file
 */
public class SettingsXMLHandler
        extends DefaultHandler
{
    private File stedSettingsFile;
    private final Map<String, String> settings = new HashMap<String, String>();
    private String key;
    private String value;
    private static final Logger logger =
            Logger.getLogger("intellibitz.sted.io.SettingsXMLHandler");

    public SettingsXMLHandler()
    {
        super();
    }

    public SettingsXMLHandler(File stedSettings)
    {
        this();
        stedSettingsFile = stedSettings;
    }

    public void read()
            throws IOException, ParserConfigurationException, SAXException
    {
        logger.entering(getClass().getName(), "read", stedSettingsFile);
        final SAXParserFactory saxParserFactory =
                SAXParserFactory.newInstance();
        saxParserFactory.setValidating(true);
        final SAXParser saxParser = saxParserFactory.newSAXParser();
        logger.info("reading settings file - " +
                stedSettingsFile.getAbsolutePath());
        final InputStream inputStream =
                FileHelper.getInputStream(stedSettingsFile);
        if (inputStream == null)
        {
            logger.severe(
                    "InputStream NULL - Cannot Read File: " + stedSettingsFile);
            throw new IOException(
                    "InputStream NULL - Cannot Read File: " + stedSettingsFile);
        }
        else
        {
            saxParser.parse(inputStream, this);
        }
        logger.exiting(getClass().getName(), "read");
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes)
            throws SAXException
    {
        if ("settings".equals(qName))
        {
        }
        else if ("option".equals(qName))
        {
            key = attributes.getValue("name");
            value = attributes.getValue("value");
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException
    {
        if ("option".equals(qName))
        {
            if (key != null)
            {
                settings.put(key, value);
            }
        }
    }

    public Map<String, String> getSettings()
    {
        return settings;
    }
}
