package intellibitz.sted.fontmap

import intellibitz.sted.event.FontMapEntriesChangeEvent
import intellibitz.sted.event.IFontMapEntriesChangeListener
import java.util.*
import javax.swing.event.EventListenerList

class FontMapEntries : ITransliterate.IEntries {
    private val word1: MutableSet<String> = TreeSet()
    private val word2: MutableSet<String> = TreeSet()
    private val words: MutableSet<String> = TreeSet()
    private val directEntries: MutableMap<String, FontMapEntry> = TreeMap()
    private val allEntries: MutableMap<String, FontMapEntry> = HashMap()
    private val ruleEntries: MutableMap<String, FontMapEntry> = TreeMap()
    val undo: java.util.Stack<FontMapEntry> = Stack()
    val redo: java.util.Stack<FontMapEntry> = Stack()
    private var fontMapEntriesChangeEvent: FontMapEntriesChangeEvent? = null
    private val fontMapEntriesChangeListeners = EventListenerList()

    fun clear() {
        directEntries.clear()
        allEntries.clear()
        ruleEntries.clear()
        word1.clear()
        word2.clear()
        words.clear()
        clearUndoRedo()
    }

    fun clearUndoRedo() {
        undo.clear()
        redo.clear()
    }

    fun reKey(oldKey: FontMapEntry, fontMapEntry: FontMapEntry) {
        remove(oldKey)
        addEntry(fontMapEntry)
    }

    fun add(entry: FontMapEntry?): Boolean {
        if (entry != null && isValid(entry)) {
            addEntry(entry)
            return true
        }
        return false
    }

    private fun addEntry(entry: FontMapEntry) {
        val key = entry.from
        val value = entry.to
        if (entry.isRulesSet()) {
            ruleEntries[entry.id] = entry
        } else {
            directEntries[key] = entry
        }
        allEntries[entry.id] = entry
        word1.add(key)
        word2.add(value)
        words.add(key)
        words.add(value)
        fireFontMapEntriesChangeEvent()
    }

    fun remove(id: String): FontMapEntry? {
        val fontMapEntry = remove(allEntries[id])
        fireFontMapEntriesChangeEvent()
        return fontMapEntry
    }

    fun remove(entry: FontMapEntry?): FontMapEntry? {
        if (entry == null) return null
        var removed = ruleEntries.remove(entry.id)
        if (removed == null) {
            removed = directEntries.remove(entry.from)
        }
        word1.remove(entry.from)
        word2.remove(entry.to)
        words.remove(entry.from)
        words.remove(entry.to)
        allEntries.remove(entry.id)
        fireFontMapEntriesChangeEvent()
        return removed
    }

    fun remove(vals: Collection<FontMapEntry>): Collection<FontMapEntry?> {
        val removedEntries = ArrayList<FontMapEntry?>(vals.size)
        for (v in vals) {
            removedEntries.add(remove(v))
        }
        fireFontMapEntriesChangeEvent()
        return removedEntries
    }

    override fun isRuleFound(word: String?): List<FontMapEntry> {
        val list = ArrayList<FontMapEntry>()
        for (entry in ruleEntries.values) {
            if (entry != null && word == entry.from) {
                list.add(entry)
            }
        }
        return list
    }

    fun isValid(entry: FontMapEntry?): Boolean {
        return entry != null && entry.isValid() &&
                (if (!entry.isRulesSet()) !directEntries.containsKey(entry.from) else true) &&
                !allEntries.containsValue(entry)
    }

    fun isValidEdit(entry: FontMapEntry?): Boolean {
        return entry != null && entry.isValid() && !allEntries.containsValue(entry)
    }

    override fun getDirectMapping(word: String?): FontMapEntry? {
        var entry = directEntries[word]
        if (entry != null) {
            entry = findDirectMapping(entry, entry)
        }
        return entry
    }

    private fun findDirectMapping(entry: FontMapEntry, root: FontMapEntry): FontMapEntry {
        val tmp = directEntries[entry.to]
        return if (tmp == null || tmp.to == root.from) {
            entry
        } else {
            findDirectMapping(tmp, root)
        }
    }

    override fun getReverseMapping(word: String?): FontMapEntry? {
        for (fontMapEntry in directEntries.values) {
            if (fontMapEntry.to == word) {
                return findReverseMapping(fontMapEntry, fontMapEntry)
            }
        }
        return null
    }

    private fun findReverseMapping(entry: FontMapEntry, root: FontMapEntry): FontMapEntry {
        var tmp: FontMapEntry? = null
        for (valEntry in directEntries.values) {
            if (valEntry.to == entry.from) {
                tmp = valEntry
                break
            }
        }
        return if (tmp == null || tmp.from == root.to) {
            entry
        } else {
            findReverseMapping(tmp, root)
        }
    }

    override fun isInWord1(word: String?): Boolean = word1.contains(word)
    override fun isInWord2(word: String?): Boolean = word2.contains(word)
    
    fun getAllWords(): Iterator<String> = words.iterator()
    fun getWord2(): Iterator<String> = word2.iterator()

    fun values(): Collection<FontMapEntry> = allEntries.values
    fun size(): Int = allEntries.size
    fun isEmpty(): Boolean = allEntries.isEmpty()

    fun addFontMapEntriesChangeListener(changeListener: IFontMapEntriesChangeListener) {
        fontMapEntriesChangeListeners.add(IFontMapEntriesChangeListener::class.java, changeListener)
    }

    fun removeFontMapEntriesChangeListener(changeListener: IFontMapEntriesChangeListener) {
        fontMapEntriesChangeListeners.remove(IFontMapEntriesChangeListener::class.java, changeListener)
    }

    private fun fireFontMapEntriesChangeEvent() {
        val listeners = fontMapEntriesChangeListeners.listenerList
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === IFontMapEntriesChangeListener::class.java) {
                if (fontMapEntriesChangeEvent == null) {
                    fontMapEntriesChangeEvent = FontMapEntriesChangeEvent(this)
                }
                (listeners[i + 1] as IFontMapEntriesChangeListener).stateChanged(fontMapEntriesChangeEvent!!)
            }
        }
    }
}
