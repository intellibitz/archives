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
 * $Id:FontMap.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/fontmap/FontMap.java $
 */

/**
 * $Id:FontMap.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/fontmap/FontMap.java $
 */

package intellibitz.sted.fontmap;

import intellibitz.sted.event.FontListChangeEvent;
import intellibitz.sted.event.FontMapChangeEvent;
import intellibitz.sted.event.FontMapChangeListener;
import intellibitz.sted.util.FileHelper;
import intellibitz.sted.util.Resources;

import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class FontMap {
    private FontMapEntries entries;
    private Font font1;
    private Font font2;
    private String font1Path = Resources.SYSTEM;
    private String font2Path = Resources.SYSTEM;
    private File fontMapFile;
    private boolean dirty;
    private FontMapChangeEvent changeEvent;
    private FontListChangeEvent fontListChangeEvent;
    private EventListenerList changeListeners;
    private EventListenerList fontListChangeListeners;
    private EventListenerList undoListeners;
    private EventListenerList redoListeners;

    private boolean console;

    private static final Logger logger =
            Logger.getLogger("intellibitz.sted.fontmap.FontMap");

    public FontMap() {
        changeListeners = new EventListenerList();
        undoListeners = new EventListenerList();
        redoListeners = new EventListenerList();
        fontListChangeListeners = new EventListenerList();
        entries = new FontMapEntries();
    }

    public FontMap(File file) {
        this();
        setFontMapFile(file);
    }

    public FontMap(File file, boolean isConsole) {
        this(file);
        setConsole(isConsole);
    }

    public void setConsole(boolean console) {
        this.console = console;
    }

    public void clear() {
        entries.clear();
        dirty = false;
        fontMapFile = null;
        font1 = null;
        font2 = null;
        font1Path = Resources.SYSTEM;
        font2Path = Resources.SYSTEM;
        changeEvent = null;
        fontListChangeEvent = null;
    }

    public String getFont1Path() {
        return font1Path;
    }

    public void setFont1Path(String font1Path) {
        this.font1Path = font1Path;
    }

    public String getFont2Path() {
        return font2Path;
    }

    public void setFont2Path(String font2Path) {
        this.font2Path = font2Path;
    }

    public FontMapEntries getEntries() {
        return entries;
    }

    /**
     * if the font is a system font.. do not create if not, check for font
     * location if font location is found, readFontMap font.. else prompt for
     * font location finally once font created, readFontMap font
     *
     * @param fontName
     */
    public void setFont1(String fontName) {
        // if running from console, no need to set fonts
        if (console) {
            return;
        }
        String font1FilePath = font1Path;
        // check if already set.. this might happen during save
        if (!fontName.equals(getFont1Name())) {
            FontInfo fontInfo = Resources.getFont(fontName);
            if (null != fontInfo) {
                font1 = fontInfo.getFont();
            }
            if (font1 != null) {
                fireFontListChangeEvent(font1, 1);
                return;
            }
            // if its not a system font, then find it from users path
/*
            if (!Resources.SYSTEM.equals(font1FilePath))
            {
                // if it not absolute path, then its the sample resource
                if (!font1FilePath.contains(File.separator)
                        ||
                        font1FilePath.contains(Resources.getResourceDirPath())
                        )
                {
                    font1FilePath = Resources.prefixResourcePath(font1FilePath);
                }
            }
*/
            // if the font is not yet loaded, then find it from users path
            File file = new File(font1FilePath);
            File file2;
            if (!file.canRead()) {
                // one last chance..  prompt again for the correct fontfile location
                file2 = FileHelper.alertAndOpenFont(fontName + " Not found in "
                                + font1FilePath +
                                ". FileDialog to choose font location will be opened now",
                        null);
                if (file2 != null && file2.canRead()) {
                    file = file2;
                }
            }
            setFont1(file);
        }
    }

    public void setFont2(String fontName) {
        // if running from console, no need to set fonts
        if (console) {
            return;
        }
        String font2FilePath = font2Path;
        // check if already set.. this might happen during save
        if (!fontName.equals(getFont2Name())) {
            FontInfo fontInfo = Resources.getFont(fontName);
            if (null != fontInfo) {
                font2 = fontInfo.getFont();
            }
            if (font2 != null) {
                fireFontListChangeEvent(font2, 2);
                return;
            }
/*
            if (!Resources.SYSTEM.equals(font2FilePath))
            {
                // if it not absolute path, then its the sample resource
                if (!font2FilePath.contains(File.separator)
                        ||
                        font2FilePath.contains(Resources.getResourceDirPath())
                        )
                {
                    font2FilePath = Resources.prefixResourcePath(font2FilePath);
                }
            }
*/
            // if the font is not yet loaded, then find it from users path
            File file = new File(font2FilePath);
            File file2;
            if (!file.canRead()) {
                // one last chance..  prompt again for the correct fontfile location
                file2 = FileHelper.alertAndOpenFont(fontName + " Not found in "
                                + font2FilePath +
                                ". FileDialog to choose font location will be opened now",
                        null);
                if (file2 != null && file2.canRead()) {
                    file = file2;
                }
            }
            setFont2(file);
        }
    }

    public void setFont1(File fontFile) {
        font1Path = fontFile.getPath();
        InputStream inputStream = null;
        try {
            inputStream = FileHelper.getInputStream(fontFile);
        } catch (FileNotFoundException e) {
            // ignore this.. we will try to readFontMap it again
        }
        try {
            if (inputStream == null) {
                // one last chance to readFontMap the font
                fontFile = FileHelper.openFont(null);
                if (fontFile != null) {
                    inputStream = FileHelper.getInputStream(fontFile);
                }
            }
            font1 = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            Font f = font1.deriveFont(Font.PLAIN, 14);
            String s = Resources.SYSTEM;
            if (null != fontFile) {
                s = fontFile.getPath();
            }
            FontInfo fontInfo = new FontInfo(f, s);
            Resources.getFonts()
                    .put(font1.getName(), fontInfo);
            fireFontListChangeEvent(font1, 1);
            logger.info("Successfully created Font " + font1);
        } catch (FontFormatException e) {
            logger.severe("Unable to Load Font.. FontFormatException: " +
                    e.getMessage());
            logger.throwing(getClass().getName(), "setFont1", e);
        } catch (IOException e) {
            logger.severe(font1Path +
                    " Unable to Load Font.. IOException: " + e.getMessage());
            logger.throwing(getClass().getName(), "setFont1", e);
        }
    }

    public void setFont2(File fontFile) {
        font2Path = fontFile.getPath();
        InputStream inputStream = null;
        try {
            inputStream = FileHelper.getInputStream(fontFile);
        } catch (FileNotFoundException e) {
            // ignore this.. we will try to readFontMap it again
        }
        try {
            if (inputStream == null) {
                fontFile = FileHelper.openFont(null);
                if (fontFile != null) {
                    inputStream = FileHelper.getInputStream(fontFile);
                }
            }
            font2 = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            Font f = font2.deriveFont(Font.PLAIN, 14);
            String s = Resources.SYSTEM;
            if (null != fontFile) {
                s = fontFile.getPath();
            }
            FontInfo fontInfo = new FontInfo(f, s);
            Resources.getFonts()
                    .put(font2.getName(), fontInfo);
            fireFontListChangeEvent(font2, 2);
            logger.info("Successfully created Font " + font2);
        } catch (FontFormatException e) {
            logger.severe("Unable to Load Font.. FontFormatException: " +
                    e.getMessage());
            logger.throwing(getClass().getName(), "setFont2", e);
        } catch (IOException e) {
            logger.severe(font2Path +
                    " Unable to Load Font.. IOException: " + e.getMessage());
            logger.throwing(getClass().getName(), "setFont2", e);
        }
    }

    private String getFont1Name() {
        return font1 == null ? Resources.EMPTY_STRING : font1.getName();
    }

    private String getFont2Name() {
        return font2 == null ? Resources.EMPTY_STRING : font2.getName();
    }

    public String getFileName() {
        if (fontMapFile != null) {
            return fontMapFile.getAbsolutePath();
        }
        return Resources.EMPTY_STRING;
    }

    public boolean isNew() {
        return Resources.EMPTY_STRING.equals(getFileName());
    }

    public Font getFont1() {
        return font1;
    }

    public Font getFont2() {
        return font2;
    }

    public void setFont1(Font font1) {
        this.font1 = font1;
    }

    public void setFont2(Font font2) {
        this.font2 = font2;
    }

    public void setFontMapFile(File file) {
        if (!file.getName().toLowerCase().endsWith(Resources.XML)) {
            fontMapFile = new File(file.getAbsolutePath() + ".xml");
        } else {
            fontMapFile = file;
        }
/*
        if (!selectedFile.exists()){
            try {
                selectedFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                logger.throwing("intellibitz.sted.util.FontMapHelper", "saveAsAction", e);
                result = JFileChooser.ERROR_OPTION;
            }
        }
        if (!this.fontMapFile.exists()) {
            throw new IllegalArgumentException("File Does not Exist: " + file.getAbsolutePath());
        }
*/
    }

    public File getFontMapFile() {
        return fontMapFile;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isReloadable() {
        return dirty && !isNew();
    }

    public boolean isFileWritable() {
        return null != fontMapFile && fontMapFile.canWrite();
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        fireFontMapEditEvent();
    }

    public void addFontMapChangeListener(FontMapChangeListener changeListener) {
        changeListeners.add(FontMapChangeListener.class, changeListener);
    }

    public void removeFontMapChangeListener(
            FontMapChangeListener changeListener) {
        changeListeners.remove(FontMapChangeListener.class, changeListener);
    }

    // Notify all listeners that have registered interest for
    // notification on this event type.  The event instance
    // is lazily created using the parameters passed into
    // the fire method.
    private void fireFontMapEditEvent() {
        // Guaranteed to return a non-null array
        final Object[] listeners = changeListeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == FontMapChangeListener.class) {
                // Lazily create the event:
                if (changeEvent == null) {
                    changeEvent = new FontMapChangeEvent(this);
                }
                ((FontMapChangeListener) listeners[i + 1])
                        .stateChanged(changeEvent);
            }
        }
    }

    public void addFontListChangeListener(ChangeListener changeListener) {
        fontListChangeListeners.add(ChangeListener.class, changeListener);
    }

    public void removeFontListChangeListener(ChangeListener changeListener) {
        fontListChangeListeners.remove(ChangeListener.class, changeListener);
    }

    // Notify all listeners that have registered interest for
    // notification on this event type.  The event instance
    // is lazily created using the parameters passed into
    // the fire method.
    private void fireFontListChangeEvent(Font font, int index) {
        // Guaranteed to return a non-null array
        final Object[] listeners = fontListChangeListeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                // Lazily create the event:
                if (fontListChangeEvent == null) {
                    fontListChangeEvent = new FontListChangeEvent(this);
                }
                fontListChangeEvent.setFontChanged(font);
                fontListChangeEvent.setFontIndex(index);
                ((ChangeListener) listeners[i + 1])
                        .stateChanged(fontListChangeEvent);
            }
        }
    }

    public void addUndoListener(FontMapChangeListener changeListener) {
        undoListeners.add(FontMapChangeListener.class, changeListener);
    }

    public void removeUndoListener(FontMapChangeListener changeListener) {
        undoListeners.remove(FontMapChangeListener.class, changeListener);
    }

    public void fireUndoEvent() {
        // Guaranteed to return a non-null array
        final Object[] listeners = undoListeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == FontMapChangeListener.class) {
                // Lazily create the event:
                if (changeEvent == null) {
                    changeEvent = new FontMapChangeEvent(this);
                }
                ((FontMapChangeListener) listeners[i + 1])
                        .stateChanged(changeEvent);
            }
        }
    }

    public void addRedoListener(FontMapChangeListener changeListener) {
        redoListeners.add(FontMapChangeListener.class, changeListener);
    }

    public void removeRedoListener(FontMapChangeListener changeListener) {
        redoListeners.remove(FontMapChangeListener.class, changeListener);
    }

    public void fireRedoEvent() {
        // Guaranteed to return a non-null array
        final Object[] listeners = redoListeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == FontMapChangeListener.class) {
                // Lazily create the event:
                if (changeEvent == null) {
                    changeEvent = new FontMapChangeEvent(this);
                }
                ((FontMapChangeListener) listeners[i + 1])
                        .stateChanged(changeEvent);
            }
        }
    }

    /**
     * @return String the string representation of this FontMap
     */
    public String toString() {
        final StringBuffer stringBuffer = new StringBuffer();
        FontInfo f = Resources.getFont(getFont1Name());
        stringBuffer.append(
                getFont1Name() + Resources.SYMBOL_ASTERISK + f.getPath());
        stringBuffer.append(Resources.NEWLINE_DELIMITER);
        f = Resources.getFont(getFont2Name());
        stringBuffer.append(
                getFont2Name() + Resources.SYMBOL_ASTERISK + f.getPath());
        stringBuffer.append(Resources.NEWLINE_DELIMITER);
        for (FontMapEntry o : getEntries().values()) {
            stringBuffer.append(o.toString());
            stringBuffer.append(Resources.NEWLINE_DELIMITER);
        }
        return stringBuffer.toString();
    }

}
