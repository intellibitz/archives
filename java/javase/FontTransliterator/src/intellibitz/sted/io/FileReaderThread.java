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
 * $Id:FileReaderThread.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/FileReaderThread.java $
 */

/**
 * $Id:FileReaderThread.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/FileReaderThread.java $
 */

package intellibitz.sted.io;

import intellibitz.sted.event.IStatusEventSource;
import intellibitz.sted.event.IStatusListener;
import intellibitz.sted.event.StatusEvent;
import intellibitz.sted.event.ThreadEventSourceBase;

import java.io.*;
import java.util.logging.Logger;

public class FileReaderThread
        extends ThreadEventSourceBase
        implements IStatusEventSource {
    private File file;

    private static final Logger logger =
            Logger.getLogger("intellibitz.sted.io.FileReaderThread");
    private IStatusListener statusListener;
    private StatusEvent statusEvent;

    public FileReaderThread() {
        super();
        statusEvent = new StatusEvent(this);
    }

    public FileReaderThread(File file) {
        this();
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public IStatusListener getStatusListener() {
        return statusListener;
    }

    public void setStatusListener(IStatusListener statusListener) {
        this.statusListener = statusListener;
    }

    public StatusEvent getStatusEvent() {
        return statusEvent;
    }

    public void setStatusEvent(StatusEvent statusEvent) {
        this.statusEvent = statusEvent;
    }

    public void run() {
        fireThreadRunStarted();
        logger.entering(getClass().getName(), "run");
        try {
            if (file == null) {
                setMessage("File is null");
                fireThreadRunFailed();
            } else {
                setResult(getFileContents(file));
                fireThreadRunFinished();
            }
        } catch (FileNotFoundException e) {
            setMessage("Cannot Read File - File not found: " + e.getMessage());
            logger.throwing(getClass().getName(), "run", e);
            fireThreadRunFailed();
        } catch (IOException e) {
            setMessage("Cannot Read File - IOException: " + e.getMessage());
            logger.throwing(getClass().getName(), "run", e);
            fireThreadRunFailed();
        }
        logger.exiting(getClass().getName(), "run");
    }

    private String getFileContents(File file)
            throws IOException {
        BufferedReader bufferedReader = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            final int sz = (int) file.length();
            final char[] cbuf = new char[sz];
            if (sz > 0) {
                bufferedReader = new BufferedReader(fileReader, sz);
                setProgressMaximum(sz);
                int count = 0;
                int len = 100;
                if (len > sz - count) {
                    len = sz - count;
                }
                int offset = count;
                logger.finest("File Size: " + sz);
                logger.finest("File Offset: " + offset);
                logger.finest("File Length to be read: " + len);
                while (bufferedReader.read(cbuf, offset, len) > 0) {
                    count += len;
                    if (len > sz - count) {
                        len = sz - count;
                    }
                    offset = count;
                    setProgress(count);
                    fireThreadRunning();
                }
            }
            return String.valueOf(cbuf);
        } finally {
            if (bufferedReader != null) {
                fileReader.close();
                bufferedReader.close();
            }
        }
    }

    public void fireStatusPosted() {
        statusListener.statusPosted(getStatusEvent());
    }

    public void addStatusListener(IStatusListener statusListener) {
        setStatusListener(statusListener);
    }
}

