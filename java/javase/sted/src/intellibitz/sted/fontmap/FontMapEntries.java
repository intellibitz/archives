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
 * $Id:FontMapEntries.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/fontmap/FontMapEntries.java $
 */

package intellibitz.sted.fontmap;

import intellibitz.sted.event.FontMapEntriesChangeEvent;
import intellibitz.sted.event.IFontMapEntriesChangeListener;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA. User: Muthu Ramadoss Date: Oct 30, 2003 Time:
 * 12:09:17 AM To change this template use Options | File Templates.
 */
public class FontMapEntries
        implements ITransliterate.IEntries
{
    // unique key
    private Set<String> word1;
    // unique value
    private Set<String> word2;
    // all words.. key + value.. unique
    private Set<String> words;
    // without rules
    private Map<String, FontMapEntry> directEntries;
    // map by id.. for faster access to entry
    private Map<String, FontMapEntry> allEntries;
    // directEntries with rules
    private Map<String, FontMapEntry> ruleEntries;
    private Stack<FontMapEntry> undo;
    private Stack<FontMapEntry> redo;
    private FontMapEntriesChangeEvent fontMapEntriesChangeEvent;
    private EventListenerList fontMapEntriesChangeListeners;

    public FontMapEntries()
    {
        directEntries = new TreeMap<String, FontMapEntry>();
        allEntries = new HashMap<String, FontMapEntry>();
        ruleEntries = new TreeMap<String, FontMapEntry>();
        word1 = new TreeSet<String>();
        word2 = new TreeSet<String>();
        words = new TreeSet<String>();
        undo = new Stack<FontMapEntry>();
        redo = new Stack<FontMapEntry>();
        fontMapEntriesChangeListeners = new EventListenerList();
    }

    /**
     * Removes all mappings from this TreeMap.
     */
    public void clear()
    {
        directEntries.clear();
        allEntries.clear();
        ruleEntries.clear();
        word1.clear();
        word2.clear();
        words.clear();
        clearUndoRedo();
    }

    public void clearUndoRedo()
    {
        undo.clear();
        redo.clear();
    }

    /**
     * Based on the edit, the collection needs to updated
     *
     * @param oldKey
     * @param fontMapEntry
     */
    public void reKey(FontMapEntry oldKey, FontMapEntry fontMapEntry)
    {
        // remove the old entry
        remove(oldKey);
        // add the new edited entry
        addEntry(fontMapEntry);
    }

    /**
     * @param entry
     */
    public boolean add(FontMapEntry entry)
    {
        // if valid entry, and entry not already added
        if (isValid(entry))
        {
            addEntry(entry);
            return true;
        }
        return false;
    }

    private void addEntry(FontMapEntry entry)
    {
        final String key = entry.getFrom();
        final String value = entry.getTo();
        if (entry.isRulesSet())
        {
            // rule entries can have the same key with different set of rules
            ruleEntries.put(entry.getId(), entry);
        }
        else
        {
            // key must be unique for entries without rules, so store by key
            directEntries.put(key, entry);
        }
        allEntries.put(entry.getId(), entry);
        word1.add(key);
        word2.add(value);
        // add all the words, key + value
        words.add(key);
        words.add(value);
        fireFontMapEntriesChangeEvent();
    }

    public FontMapEntry remove(String id)
    {
        FontMapEntry fontMapEntry = remove(allEntries.get(id));
        fireFontMapEntriesChangeEvent();
        return fontMapEntry;
    }

    /**
     * @param entry
     * @return
     */
    private FontMapEntry remove(FontMapEntry entry)
    {
        if (null == entry)
        {
            return null;
        }
        // try rule directEntries first
        FontMapEntry removed = ruleEntries.remove(entry.getId());
        // if not try blank directEntries
        if (removed == null)
        {
            removed = directEntries.remove(entry.getFrom());
        }
        word1.remove(entry.getFrom());
        word2.remove(entry.getTo());
        // remove from words
        words.remove(entry.getFrom());
        words.remove(entry.getTo());
        // remove from the master
        allEntries.remove(entry.getId());
        fireFontMapEntriesChangeEvent();
        return removed;
    }

    public Collection remove(Collection<FontMapEntry> vals)
    {
        final Iterator<FontMapEntry> iterator = vals.iterator();
        final ArrayList<FontMapEntry> removedEntries =
                new ArrayList<FontMapEntry>(vals.size());
        while (iterator.hasNext())
        {
            removedEntries.add(remove(iterator.next()));
        }
        fireFontMapEntriesChangeEvent();
        return removedEntries;
    }

    /**
     * if entry found in ruleEntries, returns true false otherwise
     *
     * @param word
     * @return
     */
    public List<FontMapEntry> isRuleFound(String word)
    {
        final Iterator<FontMapEntry> iterator = ruleEntries.values().iterator();
        final List<FontMapEntry> list = new ArrayList<FontMapEntry>();
        while (iterator.hasNext())
        {
            final FontMapEntry entry = iterator.next();
            if (entry != null && word.equals(entry.getFrom()))
            {
                list.add(entry);
            }
        }
        return list;
    }

    /**
     * @param entry
     * @return true if the entry is valid and can be added to this collection,
     *         false otherwise
     */
    public boolean isValid(FontMapEntry entry)
    {
        return entry != null && entry.isValid() &&
                (!entry.isRulesSet() ?
                        !directEntries.containsKey(entry.getFrom()) : true) &&
                !allEntries
                        .containsValue(
                                entry);
    }

    /**
     * @param entry
     * @return true if the entry is valid and can be added to this collection,
     *         false otherwise
     */
    public boolean isValidEdit(FontMapEntry entry)
    {
        return entry != null && entry.isValid() &&
                !allEntries.containsValue(entry);
    }

    public ITransliterate.IEntry getDirectMapping(String word)
    {
        FontMapEntry entry = directEntries.get(word);
        if (entry != null)
        {
            entry = findDirectMapping(entry, entry);
        }
        return entry;
    }

    private FontMapEntry findDirectMapping(FontMapEntry entry,
            FontMapEntry root)
    {
        final FontMapEntry tmp = directEntries.get(entry.getTo());
        if (tmp == null || tmp.getTo().equals(root.getFrom()))
        {
            return entry;
        }
        else
        {
            entry = findDirectMapping(tmp, root);
        }
        return entry;
    }

    public ITransliterate.IEntry getReverseMapping(String word)
    {
        // match the value for reverse transliterate
        // if value matched, then return the key
        for (FontMapEntry fontMapEntry : directEntries.values())
        {
            FontMapEntry entry = fontMapEntry;
            if (entry.getTo().equals(word))
            {
                entry = findReverseMapping(entry, entry);
                return entry;
            }
        }
        return null;
    }

    private FontMapEntry findReverseMapping(FontMapEntry entry,
            FontMapEntry root)
    {
        // match the value for reverse transliterate
        // if value matched, then return the key
        final Iterator<FontMapEntry> entries =
                directEntries.values().iterator();
        FontMapEntry tmp = null;
        while (entries.hasNext())
        {
            final FontMapEntry val = entries.next();
            if (val.getTo().equals(entry.getFrom()))
            {
                tmp = val;
                break;
            }
        }
        if (tmp == null || tmp.getFrom().equals(root.getTo()))
        {
            return entry;
        }
        else
        {
            entry = findReverseMapping(tmp, root);
        }
        return entry;
    }

    public boolean isInWord1(String word)
    {
        return word1.contains(word);
    }

    public boolean isInWord2(String word)
    {
        return word2.contains(word);
    }

    public Iterator<String> getAllWords()
    {
        return words.iterator();
    }

    public Iterator<String> getWord2()
    {
        return word2.iterator();
    }

    public Stack<FontMapEntry> getUndo()
    {
        return undo;
    }

    public Stack<FontMapEntry> getRedo()
    {
        return redo;
    }

    public Collection<FontMapEntry> values()
    {
        return allEntries.values();
    }

    public int size()
    {
        return allEntries.size();
    }

    public boolean isEmpty()
    {
        return allEntries.isEmpty();
    }

    public void addFontMapEntriesChangeListener(
            IFontMapEntriesChangeListener changeListener)
    {
        fontMapEntriesChangeListeners
                .add(IFontMapEntriesChangeListener.class, changeListener);
    }

    public void removeFontMapEntriesChangeListener(
            IFontMapEntriesChangeListener changeListener)
    {
        fontMapEntriesChangeListeners
                .remove(IFontMapEntriesChangeListener.class, changeListener);
    }

    // Notify all listeners that have registered interest for
    // notification on this event type.  The event instance
    // is lazily created using the parameters passed into
    // the fire method.
    private void fireFontMapEntriesChangeEvent()
    {
        // Guaranteed to return a non-null array
        final Object[] listeners =
                fontMapEntriesChangeListeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == IFontMapEntriesChangeListener.class)
            {
                // Lazily create the event:
                if (fontMapEntriesChangeEvent == null)
                {
                    fontMapEntriesChangeEvent =
                            new FontMapEntriesChangeEvent(this);
                }
                ((IFontMapEntriesChangeListener) listeners[i + 1])
                        .stateChanged(fontMapEntriesChangeEvent);
            }
        }
    }


}
