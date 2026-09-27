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
 * $Id:STEDConsole.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/launch/STEDConsole.java $
 */

package intellibitz.sted.launch;

import intellibitz.sted.event.IThreadListener;
import intellibitz.sted.event.ThreadEvent;
import intellibitz.sted.fontmap.Converter;
import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.io.FontMapReader;
import intellibitz.sted.util.Resources;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;


public class STEDConsole
        implements IThreadListener
{
    private String fontMapName;
    private String inputFileName;
    private String outputFileName;
    private boolean reverse;
    private boolean html;
    private static Logger logger;

    public STEDConsole(List<String> args)
    {
        logger = Logger.getLogger("intellibitz.sted.launch.STEDConsole");
        STEDLogManager.getLogmanager().addLogger(logger);
        loadArgs(args);
        if (null == fontMapName || Resources.EMPTY_STRING.equals(fontMapName))
        {
            logger.info("Invalid FontMap: " + fontMapName);
            printUsage();
            System.exit(1);
        }
        if (null == inputFileName ||
                Resources.EMPTY_STRING.equals(inputFileName))
        {
            logger.info("Invalid Input File: " + inputFileName);
            printUsage();
            System.exit(2);
        }
        if (null == outputFileName ||
                Resources.EMPTY_STRING.equals(outputFileName))
        {
            logger.info("Invalid Output File: " + outputFileName);
            printUsage();
            System.exit(3);
        }
        final File input = new File(inputFileName);
        final File output = new File(outputFileName);
        // create console based FontMap.. no need to readFontMap fonts
        final FontMap fontMap = new FontMap(new File(fontMapName), true);
        try
        {
            FontMapReader.read(fontMap);
            final Converter converter = new Converter(fontMap, input, output);
            converter.setReverseTransliterate(reverse);
            converter.setHTMLAware(html);
            converter.addThreadListener(this);
            logger.info("Running Transliterator with the following options: ");
            logger.info("   FontMap: " + fontMapName);
            logger.info("   Input: " + inputFileName);
            logger.info("   Output: " + outputFileName);
            logger.info("   Preserve Tags in Transliteration: " + html);
            logger.info("   Reverse Transliteration: " + reverse);
            converter.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            logger.severe("Exception : " + e.getMessage());
            logger.throwing("intellibitz.sted.launch.STEDConsole",
                    "Constructor", e);
            System.exit(-1);
        }
        catch (SAXException e)
        {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            logger.severe("Exception : " + e.getMessage());
            logger.throwing("intellibitz.sted.launch.STEDConsole",
                    "Constructor", e);
            System.exit(-1);
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            logger.severe("Exception : " + e.getMessage());
            logger.throwing("intellibitz.sted.launch.STEDConsole",
                    "Constructor", e);
            System.exit(-1);
        }
    }

    public static void main(String[] args)
    {
        try
        {
            new STEDConsole(Arrays.asList(args));
        }
        catch (Exception e)
        {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            logger.severe("Exception : " + e.getMessage());
            logger.throwing("intellibitz.sted.launch.STEDConsole", "main", e);
            System.exit(-1);
        }
        finally
        {
        }
    }

    private static void printUsage()
    {
        logger.info("STED Console Usage: ");
        logger.info(
                "   java -Dfontmap.file='<file>' -Dinput.file='<input>' -Doutput.file='<output>' intellibitz.sted.launch.STEDConsole");
        logger.info(" -OR- ");
        logger.info(
                "   java intellibitz.sted.launch.STEDConsole -map='<file>' -in='<input>' -out='<output>'");
    }

    private void loadArgs(List<String> args)
    {
        for (final String param : args)
        {
            if (param.startsWith("-map="))
            {
                fontMapName = param.substring(5);
            }
            else if (param.startsWith("-in="))
            {
                inputFileName = param.substring(4);
            }
            else if (param.startsWith("-out="))
            {
                outputFileName = param.substring(5);
            }
        }
        reverse = args.contains("-r") || args.contains("-R");
        html = args.contains("-p") || args.contains("-P");
        if (fontMapName == null)
        {
            fontMapName = System.getProperty(Resources.FONTMAP_FILE);
        }
        if (inputFileName == null)
        {
            inputFileName = System.getProperty(Resources.INPUT_FILE);
        }
        if (outputFileName == null)
        {
            outputFileName = System.getProperty(Resources.OUTPUT_FILE);
        }
    }

    public void threadRunStarted(ThreadEvent e)
    {

    }

    public void threadRunning(ThreadEvent e)
    {

    }

    public void threadRunFailed(ThreadEvent e)
    {
        logger.severe(e.getEventSource().getMessage().toString());
        System.exit(-1);
    }

    public void threadRunFinished(ThreadEvent e)
    {
        logger.info(e.getEventSource().getMessage().toString());
        System.exit(0);
    }
}
