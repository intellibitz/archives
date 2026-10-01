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
 * $Id:FontMapEntries.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/fontmap/FontMapEntries.kt $
 */

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
