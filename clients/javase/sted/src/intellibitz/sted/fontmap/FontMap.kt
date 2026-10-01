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
 * $Id:FontMap.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/fontmap/FontMap.kt $
 */

package intellibitz.sted.fontmap

import intellibitz.sted.event.FontListChangeEvent
import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.util.FileHelper
import intellibitz.sted.util.Resources
import java.awt.Font
import java.awt.FontFormatException
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.logging.Logger
import javax.swing.event.ChangeListener
import javax.swing.event.EventListenerList

class FontMap {
    val entries: FontMapEntries = FontMapEntries()
    var font1: Font? = null
    var font2: Font? = null
    var font1Path: String = Resources.SYSTEM
    var font2Path: String = Resources.SYSTEM
    var fontMapFile: File? = null
        set(value) {
            if (value != null) {
                field = if (!value.name.lowercase().endsWith(Resources.XML)) {
                    File(value.absolutePath + ".xml")
                } else {
                    value
                }
            } else {
                field = null
            }
        }
    var isDirty: Boolean = false
        set(value) {
            field = value
            fireFontMapEditEvent()
        }
    var isConsole: Boolean = false

    private var changeEvent: FontMapChangeEvent? = null
    private var fontListChangeEvent: FontListChangeEvent? = null
    private val changeListeners = EventListenerList()
    private val fontListChangeListeners = EventListenerList()
    private val undoListeners = EventListenerList()
    private val redoListeners = EventListenerList()

    companion object {
        private val logger = Logger.getLogger("intellibitz.sted.fontmap.FontMap")
    }

    constructor()

    constructor(file: File) : this() {
        fontMapFile = file
    }

    constructor(file: File, isConsole: Boolean) : this(file) {
        this.isConsole = isConsole
    }

    fun clear() {
        entries.clear()
        isDirty = false
        fontMapFile = null
        font1 = null
        font2 = null
        font1Path = Resources.SYSTEM
        font2Path = Resources.SYSTEM
        changeEvent = null
        fontListChangeEvent = null
    }

    fun setFont1(fontName: String) {
        if (isConsole) return
        var font1FilePath = font1Path
        if (fontName != getFont1Name()) {
            val fontInfo = Resources.getFont(fontName)
            if (fontInfo != null) {
                font1 = fontInfo.font
            }
            if (font1 != null) {
                fireFontListChangeEvent(font1, 1)
                return
            }
            var file = File(font1FilePath)
            if (!file.canRead()) {
                val file2 = FileHelper.alertAndOpenFont(
                    "$fontName Not found in $font1FilePath. FileDialog to choose font location will be opened now", null as java.awt.Component?
                )
                if (file2 != null && file2.canRead()) {
                    file = file2
                }
            }
            setFont1(file)
        }
    }

    fun setFont2(fontName: String) {
        if (isConsole) return
        var font2FilePath = font2Path
        if (fontName != getFont2Name()) {
            val fontInfo = Resources.getFont(fontName)
            if (fontInfo != null) {
                font2 = fontInfo.font
            }
            if (font2 != null) {
                fireFontListChangeEvent(font2, 2)
                return
            }
            var file = File(font2FilePath)
            if (!file.canRead()) {
                val file2 = FileHelper.alertAndOpenFont(
                    "$fontName Not found in $font2FilePath. FileDialog to choose font location will be opened now", null as java.awt.Component?
                )
                if (file2 != null && file2.canRead()) {
                    file = file2
                }
            }
            setFont2(file)
        }
    }

    fun setFont1(fontFile: File?) {
        if (fontFile == null) return
        font1Path = fontFile.path
        var inputStream = try {
            FileHelper.getInputStream(fontFile)
        } catch (e: FileNotFoundException) {
            null
        }
        try {
            var actualFontFile = fontFile
            if (inputStream == null) {
                actualFontFile = FileHelper.openFont(null as java.awt.Component?)
                if (actualFontFile != null) {
                    inputStream = FileHelper.getInputStream(actualFontFile)
                }
            }
            if (inputStream != null) {
                font1 = Font.createFont(Font.TRUETYPE_FONT, inputStream)
                val f = font1!!.deriveFont(Font.PLAIN, 14f)
                val s = actualFontFile?.path ?: Resources.SYSTEM
                val fontInfo = FontInfo(f, s)
                Resources.fonts[font1!!.name] = fontInfo
                fireFontListChangeEvent(font1, 1)
                logger.info("Successfully created Font $font1")
            }
        } catch (e: FontFormatException) {
            logger.severe("Unable to Load Font.. FontFormatException: ${e.message}")
            logger.throwing(javaClass.name, "setFont1", e)
        } catch (e: IOException) {
            logger.severe("$font1Path Unable to Load Font.. IOException: ${e.message}")
            logger.throwing(javaClass.name, "setFont1", e)
        }
    }

    fun setFont2(fontFile: File?) {
        if (fontFile == null) return
        font2Path = fontFile.path
        var inputStream = try {
            FileHelper.getInputStream(fontFile)
        } catch (e: FileNotFoundException) {
            null
        }
        try {
            var actualFontFile = fontFile
            if (inputStream == null) {
                actualFontFile = FileHelper.openFont(null as java.awt.Component?)
                if (actualFontFile != null) {
                    inputStream = FileHelper.getInputStream(actualFontFile)
                }
            }
            if (inputStream != null) {
                font2 = Font.createFont(Font.TRUETYPE_FONT, inputStream)
                val f = font2!!.deriveFont(Font.PLAIN, 14f)
                val s = actualFontFile?.path ?: Resources.SYSTEM
                val fontInfo = FontInfo(f, s)
                Resources.fonts[font2!!.name] = fontInfo
                fireFontListChangeEvent(font2, 2)
                logger.info("Successfully created Font $font2")
            }
        } catch (e: FontFormatException) {
            logger.severe("Unable to Load Font.. FontFormatException: ${e.message}")
            logger.throwing(javaClass.name, "setFont2", e)
        } catch (e: IOException) {
            logger.severe("$font2Path Unable to Load Font.. IOException: ${e.message}")
            logger.throwing(javaClass.name, "setFont2", e)
        }
    }

    private fun getFont1Name(): String = font1?.name ?: Resources.EMPTY_STRING
    private fun getFont2Name(): String = font2?.name ?: Resources.EMPTY_STRING

    fun getFileName(): String = fontMapFile?.absolutePath ?: Resources.EMPTY_STRING
    fun isNew(): Boolean = getFileName() == Resources.EMPTY_STRING
    fun isReloadable(): Boolean = isDirty && !isNew()
    fun isFileWritable(): Boolean = fontMapFile?.canWrite() ?: false

    fun addFontMapChangeListener(changeListener: FontMapChangeListener) {
        changeListeners.add(FontMapChangeListener::class.java, changeListener)
    }

    fun removeFontMapChangeListener(changeListener: FontMapChangeListener) {
        changeListeners.remove(FontMapChangeListener::class.java, changeListener)
    }

    private fun fireFontMapEditEvent() {
        val listeners = changeListeners.listenerList
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === FontMapChangeListener::class.java) {
                if (changeEvent == null) {
                    changeEvent = FontMapChangeEvent(this)
                }
                (listeners[i + 1] as FontMapChangeListener).stateChanged(changeEvent!!)
            }
        }
    }

    fun addFontListChangeListener(changeListener: ChangeListener) {
        fontListChangeListeners.add(ChangeListener::class.java, changeListener)
    }

    fun removeFontListChangeListener(changeListener: ChangeListener) {
        fontListChangeListeners.remove(ChangeListener::class.java, changeListener)
    }

    private fun fireFontListChangeEvent(font: Font?, index: Int) {
        val listeners = fontListChangeListeners.listenerList
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === ChangeListener::class.java) {
                if (fontListChangeEvent == null) {
                    fontListChangeEvent = FontListChangeEvent(this)
                }
                fontListChangeEvent!!.fontChanged = font
                fontListChangeEvent!!.fontIndex = index
                (listeners[i + 1] as ChangeListener).stateChanged(fontListChangeEvent)
            }
        }
    }

    fun addUndoListener(changeListener: FontMapChangeListener) {
        undoListeners.add(FontMapChangeListener::class.java, changeListener)
    }

    fun removeUndoListener(changeListener: FontMapChangeListener) {
        undoListeners.remove(FontMapChangeListener::class.java, changeListener)
    }

    fun fireUndoEvent() {
        val listeners = undoListeners.listenerList
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === FontMapChangeListener::class.java) {
                if (changeEvent == null) {
                    changeEvent = FontMapChangeEvent(this)
                }
                (listeners[i + 1] as FontMapChangeListener).stateChanged(changeEvent!!)
            }
        }
    }

    fun addRedoListener(changeListener: FontMapChangeListener) {
        redoListeners.add(FontMapChangeListener::class.java, changeListener)
    }

    fun removeRedoListener(changeListener: FontMapChangeListener) {
        redoListeners.remove(FontMapChangeListener::class.java, changeListener)
    }

    fun fireRedoEvent() {
        val listeners = redoListeners.listenerList
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === FontMapChangeListener::class.java) {
                if (changeEvent == null) {
                    changeEvent = FontMapChangeEvent(this)
                }
                (listeners[i + 1] as FontMapChangeListener).stateChanged(changeEvent!!)
            }
        }
    }

    override fun toString(): String {
        val stringBuffer = StringBuilder()
        var f = Resources.getFont(getFont1Name())
        stringBuffer.append(getFont1Name()).append(Resources.SYMBOL_ASTERISK).append(f?.path ?: "")
        stringBuffer.append(Resources.NEWLINE_DELIMITER)
        
        f = Resources.getFont(getFont2Name())
        stringBuffer.append(getFont2Name()).append(Resources.SYMBOL_ASTERISK).append(f?.path ?: "")
        stringBuffer.append(Resources.NEWLINE_DELIMITER)
        
        for (o in entries.values()) {
            stringBuffer.append(o.toString())
            stringBuffer.append(Resources.NEWLINE_DELIMITER)
        }
        return stringBuffer.toString()
    }
}
