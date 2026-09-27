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
 * $Id:Converter.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/fontmap/Converter.java $
 */

/**
 * $Id:Converter.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/fontmap/Converter.java $
 */

package intellibitz.sted.fontmap;

import intellibitz.sted.event.ThreadEventSourceBase;
import intellibitz.sted.event.TransliterateEvent;
import intellibitz.sted.util.FileHelper;

import java.io.*;
import java.util.logging.Logger;

public class Converter
        extends ThreadEventSourceBase {
    private boolean stopRequested;
    private FontMap fontMap;
    private File fileToConvert;
    private File convertedFile;
    private boolean initialized;
    private boolean success;
    private static final Logger logger =
            Logger.getLogger("intellibitz.sted.fontmap.Converter");
    private ITransliterate transliterate;

    public Converter() {
        super();
        setThreadEvent(new TransliterateEvent(this));
        initTransliterator();
    }

    public Converter(FontMap fontMap, File input, File output) {
        this();
        init(fontMap, input, output);
    }

    private void init(FontMap fontMap, File input, File output) {
        fileToConvert = input;
        convertedFile = output;
        initialized = true;
        success = false;
        stopRequested = false;
        setFontMap(fontMap);
    }

    private void initTransliterator() {
        if (transliterate == null) {
            transliterate = new DefaultTransliterator();
        }
    }

    public ITransliterate getTransliterate() {
        return transliterate;
    }

    public void setTransliterate(ITransliterate transliterate) {
        this.transliterate = transliterate;
    }

    public void run() {
        fireThreadRunStarted();
        if (!initialized) {
            throw new IllegalThreadStateException(
                    "Thread should be initialized.. Call init method before invoking run");
        }
        convertFile();
        initialized = false;
        if (success) {
            fireThreadRunFinished();
        } else {
            fireThreadRunFailed();
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    boolean isReady() {
        return fontMap != null && !fontMap.getEntries().isEmpty();
    }

    public void setHTMLAware(boolean flag) {
        transliterate.setHTMLAware(flag);
    }

    private void convertFile() {
        try {
            FileHelper.fileCopy(fileToConvert,
                    fileToConvert.getAbsolutePath() + ".bakup");
        } catch (IOException e) {
            setMessage("Unable to Backup input file: " + e.getMessage());
            success = false;
            logger.severe("Unable to Backup input file: " + e.getMessage());
            logger.throwing(getClass().getName(), "convertFile", e);
            return;
        }
        final BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(fileToConvert));
        } catch (FileNotFoundException e) {
            setMessage("File Not Found: " + e.getMessage());
            success = false;
            logger.severe("Cannot Read - File Not Found: " + e.getMessage());
            logger.throwing(getClass().getName(), "convertFile", e);
            return;
        }
        final BufferedWriter bufferedWriter;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(convertedFile));
        } catch (IOException e) {
            setMessage("Cannot create Writer: " + e.getMessage());
            success = false;
            logger.severe("Cannot Write - IOException: " + e.getMessage());
            logger.throwing(getClass().getName(), "convertFile", e);
            return;
        }
        String input;
        try {
            while ((input = bufferedReader.readLine()) != null) {
                if (stopRequested) {
                    break;
                }
                bufferedWriter.write(transliterate.parseLine(input));
                bufferedWriter.newLine();
                fireThreadRunning();
            }
        } catch (IOException e) {
            setMessage("IOException: " + e.getMessage());
            success = false;
            logger.severe(
                    "IOException - Ceasing Conversion: " + e.getMessage());
            logger.throwing(getClass().getName(), "convertFile", e);
            return;
        } finally {
            try {
                bufferedReader.close();
                bufferedWriter.close();
            } catch (IOException e) {
                setMessage("Cannot Close Reader/Writer - IOException: " +
                        e.getMessage());
                success = false;
                logger.severe(
                        "Cannot close File Streams - Ceasing Conversion: " +
                                e.getMessage());
                logger.throwing(getClass().getName(), "convertFile", e);
                return;
            }
        }
        if (!stopRequested) {
            setMessage("Transliterate Done.");
            success = true;
        }
    }

    public void setReverseTransliterate(boolean flag) {
        transliterate.setReverseTransliterate(flag);
    }

    public void setFontMap(FontMap fontMap) {
        this.fontMap = fontMap;
        if (fontMap != null) {
            transliterate.setEntries(fontMap.getEntries());
        }
    }

    public synchronized void setStopRequested(boolean flag) {
        stopRequested = flag;
    }
}

