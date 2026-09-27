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
 * $Id:DesktopModel.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/DesktopModel.java $
 */

/**
 * $Id:DesktopModel.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/DesktopModel.java $
 */

package intellibitz.sted.ui;

import intellibitz.sted.event.FontMapChangeEvent;
import intellibitz.sted.event.FontMapChangeListener;
import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.io.FontMapXMLWriter;

import javax.swing.event.EventListenerList;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.util.logging.Logger;


/**
 * Contains FontMap and all its related Entities.
 */
public class DesktopModel {
    private File inputFile;
    private File outputFile;
    private FontMap fontMap;

    // the fontmap related events.. the model when change will fire these
    private FontMapChangeEvent fontMapChangeEvent;
    private EventListenerList eventListenerList;
    private static final Logger logger =
            Logger.getLogger(DesktopModel.class.getName());

    public DesktopModel() {
        super();
        eventListenerList = new EventListenerList();
    }


    public void clear() {
        fontMapChangeEvent = null;
    }

    // Notify all listeners that have registered interest for
    // notification on this event type.  The event instance
    // is lazily created using the parameters passed into
    // the fire method.
    public void fireFontMapChangedEvent() {
        // Guaranteed to return a non-null array
        final Object[] listeners = eventListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == FontMapChangeListener.class) {
                // Lazily create the event:
                if (fontMapChangeEvent == null) {
                    fontMapChangeEvent = new FontMapChangeEvent(getFontMap());
                }
                ((FontMapChangeListener) listeners[i + 1])
                        .stateChanged(fontMapChangeEvent);
            }
        }
    }

    public FontMap getFontMap() {
        return fontMap;
    }

    public void setFontMap(FontMap fontMap) {
        this.fontMap = fontMap;
    }

    public boolean isReadyForTransliteration
            () {
        boolean flag = false;
        if (getInputFile() != null
                && getOutputFile() != null
                && getFontMap().getFontMapFile() != null) {
            flag = true;
        }
        return flag;
    }


    public File getInputFile
            () {
        return inputFile;
    }

    public File getOutputFile
            () {
        return outputFile;
    }

    public void setInputFile
            (File file) {
        inputFile = file;
    }


    public void setOutputFile(File file) {
        outputFile = file;
    }

    public void addFontMapChangeListener
            (FontMapChangeListener fontMapChangeListener) {
        eventListenerList
                .add(FontMapChangeListener.class, fontMapChangeListener);
    }

    public void removeFontMapChangeListener
            (FontMapChangeListener fontMapChangeListener) {
        eventListenerList
                .remove(FontMapChangeListener.class, fontMapChangeListener);
    }


    public FontMap saveFontMap()
            throws TransformerException {
        FontMapXMLWriter.write(fontMap);
        fontMap.getEntries().clearUndoRedo();
        fontMap.setDirty(false);
        fireFontMapChangedEvent();
        return fontMap;
    }

}