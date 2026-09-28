package intellibitz.sted.ui

import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.io.FontMapXMLWriter
import java.io.File
import java.util.logging.Logger
import javax.swing.event.EventListenerList
import javax.xml.transform.TransformerException

class DesktopModel {
    var inputFile: File? = null
    var outputFile: File? = null
    var fontMap: FontMap? = null

    private var fontMapChangeEvent: FontMapChangeEvent? = null
    private val eventListenerList: EventListenerList = EventListenerList()

    fun clear() {
        fontMapChangeEvent = null
    }

    fun fireFontMapChangedEvent() {
        val listeners = eventListenerList.listenerList
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === FontMapChangeListener::class.java) {
                if (fontMapChangeEvent == null) {
                    fontMapChangeEvent = FontMapChangeEvent(fontMap!!)
                }
                (listeners[i + 1] as FontMapChangeListener).stateChanged(fontMapChangeEvent!!)
            }
        }
    }

    val isReadyForTransliteration: Boolean
        get() = inputFile != null && outputFile != null && fontMap?.fontMapFile != null

    fun addFontMapChangeListener(fontMapChangeListener: FontMapChangeListener) {
        eventListenerList.add(FontMapChangeListener::class.java, fontMapChangeListener)
    }

    fun removeFontMapChangeListener(fontMapChangeListener: FontMapChangeListener) {
        eventListenerList.remove(FontMapChangeListener::class.java, fontMapChangeListener)
    }

    @Throws(TransformerException::class)
    fun saveFontMap(): FontMap? {
        if (fontMap != null) {
            FontMapXMLWriter.write(fontMap!!)
            fontMap!!.entries.clearUndoRedo()
            fontMap!!.isDirty = false
            fireFontMapChangedEvent()
        }
        return fontMap
    }

    companion object {
        private val logger = Logger.getLogger(DesktopModel::class.java.name)
    }
}
