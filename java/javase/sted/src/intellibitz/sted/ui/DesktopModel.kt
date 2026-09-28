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
 * $Id:DesktopModel.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/DesktopModel.kt $
 */

package intellibitz.sted.ui

import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.io.FontMapXMLWriter
import javax.swing.event.EventListenerList
import javax.xml.transform.TransformerException
import java.io.File
import java.util.logging.Logger

class DesktopModel {
    private var inputFile: File? = null
    private var outputFile: File? = null
    private var fontMap: FontMap? = null
    private var fontMapChangeEvent: FontMapChangeEvent? = null
    private var eventListenerList: EventListenerList? = null
    constructor() : super() {
        eventListenerList = EventListenerList()
    }
    fun clear() {
        fontMapChangeEvent = null
    }
    fun fireFontMapChangedEvent() {
        val listeners: Array<Object> = eventListenerList.getListenerList()
        var i: Int = (listeners.length - 2)
        while ((i >= 0)) {
            if ((listeners[i] == FontMapChangeListener::class.java)) {
                if ((fontMapChangeEvent == null)) {
                    fontMapChangeEvent = FontMapChangeEvent(getFontMap())
                }
                (listeners[(i + 1)] as FontMapChangeListener)
            }
            i -= 2
        }
    }
    fun getFontMap(): FontMap {
        return fontMap
    }
    fun setFontMap(fontMap: FontMap) {
        this = fontMap
    }
    fun isReadyForTransliteration(): Boolean {
        var flag: Boolean = false
        if ((((getInputFile() != null) && (getOutputFile() != null)) && (getFontMap() != null))) {
            flag = true
        }
        return flag
    }
    fun getInputFile(): File {
        return inputFile
    }
    fun getOutputFile(): File {
        return outputFile
    }
    fun setInputFile(file: File) {
        inputFile = file
    }
    fun setOutputFile(file: File) {
        outputFile = file
    }
    fun addFontMapChangeListener(fontMapChangeListener: FontMapChangeListener) {
        eventListenerList.add(FontMapChangeListener::class.java, fontMapChangeListener)
    }
    fun removeFontMapChangeListener(fontMapChangeListener: FontMapChangeListener) {
        eventListenerList.remove(FontMapChangeListener::class.java, fontMapChangeListener)
    }
    @Throws(TransformerException::class)
    fun saveFontMap(): FontMap {
        FontMapXMLWriter.write(fontMap)
        fontMap.getEntries()
        fontMap.setDirty(false)
        fireFontMapChangedEvent()
        return fontMap
    }

    companion object {
        private val logger: Logger = Logger.getLogger(DesktopModel::class.java)
    }
}
