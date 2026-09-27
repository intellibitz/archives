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
 * $Id:DefaultTransliterator.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/fontmap/DefaultTransliterator.java $
 */

package intellibitz.sted.fontmap;

import intellibitz.sted.util.Resources;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class DefaultTransliterator
        implements ITransliterate
{
    private boolean reverseTransliterate;
    private boolean isHTMLAware = true;
    private boolean isParseMode = true;
    private IEntries entries;

    private int converted;
    private String prevWord;

    public DefaultTransliterator()
    {
        super();
    }

    public void setEntries(ITransliterate.IEntries entries)
    {
        this.entries = entries;
    }

    public void setReverseTransliterate(boolean flag)
    {
        reverseTransliterate = flag;
    }

    public void setHTMLAware(boolean flag)
    {
        isHTMLAware = flag;
    }

    public String parseLine(String input)
    {
        final StringBuffer output = new StringBuffer();
        final StringTokenizer stringTokenizer =
                new StringTokenizer(input, " ", true);
        while (stringTokenizer.hasMoreTokens())
        {
            final String word = stringTokenizer.nextToken();
            // break the word into more word, for HTML parsing
            final StringTokenizer st = new StringTokenizer(word,
                    Resources
                            .HTML_TAG_START +
                            Resources
                                    .HTML_TAG_END
                            +
                            Resources
                                    .HTML_TAG_START_ESCAPE +
                            Resources
                                    .HTML_TAG_END_ESCAPE
                            +
                            Resources
                                    .SPACE,
                    true);
            while (st.hasMoreTokens())
            {
                final String currWord = st.nextToken();
                parseWord(currWord, prevWord, output);
                prevWord = currWord;
            }
        }
        return output.toString();
    }

    private void parseWord(String word, String prevWord, StringBuffer output)
    {
        // if its a space, do not parse
        if (Resources.SPACE.equals(word))
        {
            output.append(word);
            return;
        }
        if ((Resources.HTML_TAG_START.equals(word)
                || Resources.HTML_TAG_START_ESCAPE.equals(word))
                && isHTMLAware)
        {
            isParseMode = false;
            output.append(word);
            return;
        }
        else if ((Resources.HTML_TAG_END.equals(word) ||
                Resources.HTML_TAG_END_ESCAPE.equals(word))
                && isHTMLAware)
        {
            isParseMode = true;
            if (Resources.HTML_TAG_END_ESCAPE.equals(word) &&
                    "lt".equals(prevWord))
            {
                isParseMode = false;
            }
            output.append(word);
            return;
        }
        // if its not in parse mode, skip parsing, unless an html end character is hit
        if (!(Resources.HTML_TAG_END.equals(word) ||
                Resources.HTML_TAG_END_ESCAPE.equals(word))
                && !isParseMode)
        {
            output.append(word);
            return;
        }
        converted = 0;
        convertWord(word, output, word, word.length(), Resources.EMPTY_STRING,
                Resources.EMPTY_STRING, word);
    }

    private void convertWord(String word, StringBuffer output, String chopped,
            int wordLen, String translated, String leftover,
            String original)
    {
        if (converted == wordLen)
        {
            return;
        }
        // translate the whole word, in one shot, if possible
        if (translate(word, output, translated, leftover, original))
        {
            converted += word.length();
        }
        else
        {
            String remaining = word;
            // chop the last char from the word, and try the remaining word first
            if (word.length() > 1)
            {
                remaining = word.substring(0, word.length() - 1);
                // get the last char
                leftover = word.substring(word.length() - 1, word.length()) +
                        leftover;
            }
            convertWord(remaining, output, chopped, wordLen, translated,
                    leftover, original);
            translated += remaining;
            // now try the leftover remaining word
            final String remaining2 = chopped.substring(remaining.length());
            leftover = Resources.EMPTY_STRING;
            convertWord(remaining2, output, remaining2, wordLen, translated,
                    leftover, original);
        }
    }

    private boolean translate(String word, StringBuffer output,
            String translated,
            String leftover, String original)
    {
        final char[] result;
        if (isParseMode)
        {
            result = translateWord(word, translated, leftover, original);
        }
        else
        {
            result = word.toCharArray();
        }
        if (result != null)
        {
            output.append(result);
            return true;
        }
        return false;
    }

    /**
     * for forward translate looks in the values , for backward translate looks
     * in the keyset
     *
     * @param word       is the _inputFileSelectorPanel character word
     * @param translated
     * @param leftover
     * @param original
     * @return char[] the translated character word
     */
    private char[] translateWord(String word, String translated,
            String leftover,
            String original)
    {
        final String wordToConvert =
                applyIndirectMappingIfAny(word, translated, leftover, original);
        char[] chars = null;
        // if the word is translatable
        if (isWordMapped(wordToConvert))
        {
            if (reverseTransliterate)
            {
                final IEntry entry = entries.getReverseMapping(wordToConvert);
                chars = entry == null ? wordToConvert.toCharArray() :
                        entry.getFrom().toCharArray();
            }
            else
            {
                final IEntry entry = entries.getDirectMapping(wordToConvert);
                chars = entry == null ? wordToConvert.toCharArray() :
                        entry.getTo().toCharArray();
            }
        }
        // if only 1 character and still not mapped, then is not in language, so return the char
        // if not return, coz the word can be broken into letters and then try to map again
        if (chars == null && wordToConvert.length() == 1)
        {
            chars = new char[]{wordToConvert.charAt(0)};
        }
        return chars;
    }

    private String applyIndirectMappingIfAny(String word, String translated,
            String leftover, String template)
    {
        String result = word;
        List<FontMapEntry> list = entries.isRuleFound(word);
        // if rule fontMap.getEntries () are found for the word
        if (!list.isEmpty())
        {
            final Iterator<FontMapEntry> iterator = list.iterator();
            result = word;
            while (iterator.hasNext())
            {
                final FontMapEntry entry = iterator.next();
                result = indirectMap(entry, result, translated, leftover,
                        template);
            }
        }
        // if its a translatable word.. then try to find chars within the word and map them
        else if (entries.isInWord1(word))
        {
            int len = word.length();
            while (len > 0)
            {
                final String nword = result.substring(0, len--);
                list = entries.isRuleFound(nword);
                // if rule entries are found for the word
                if (!list.isEmpty())
                {
                    final Iterator<FontMapEntry> iterator = list.iterator();
                    result = word;
                    while (iterator.hasNext())
                    {
                        final FontMapEntry entry = iterator.next();
                        if (shouldBeginsWithIndirectMappingApplied(entry,
                                translated,
                                template)
                                || shouldPrecededByIndirectMappingApplied(entry,
                                translated))
                        {
                            result = indirectMap(entry, result, translated,
                                    leftover, template);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * @param entry
     * @param word
     * @param translated
     * @param leftover
     * @param template
     * @return
     */
    private static String indirectMap(FontMapEntry entry, String word,
            String translated, String leftover,
            String template)
    {
        String result = word;
        if (shouldBeginsWithIndirectMappingApplied(entry, translated, template))
        {
            // if the mapping is already present, then nothing to replace
            if (!template.startsWith(entry.getTo()))
            {
                result = word.replaceFirst(entry.getFrom(), entry.getTo());
            }
        }
        if (entry.isEndsWith() && (translated != null && template.length() -
                translated.length() ==
                1))
        {
            // if the mapping is already present, then nothing to replace
            if (!template.endsWith(entry.getTo()))
            {
                result = word.replaceFirst(entry.getFrom(), entry.getTo());
            }
        }
        if (entry.getFollowedBy() != null &&
                entry.getFollowedBy().length() > 0 &&
                leftover.startsWith(entry.getFollowedBy()))
        {
            // if the mapping is already present, then nothing to replace
            if (word.equals(entry.getFrom())
                    ||
                    word.length() >= entry.getTo().length() &&
                            !word.substring(word.indexOf(entry.getFrom()),
                                    entry.getTo().length())
                                    .equals(entry.getTo()))
            {
                result = word.replaceFirst(entry.getFrom(), entry.getTo());
            }
        }
        if (shouldPrecededByIndirectMappingApplied(entry, translated))
        {
            // if the mapping is already present, then nothing to replace
            if (word.equals(entry.getFrom())
                    ||
                    word.length() >= entry.getTo().length() &&
                            !word.substring(word.indexOf(entry.getFrom()),
                                    entry.getTo().length())
                                    .equals(entry.getTo()))
            {
                result = word.replaceFirst(entry.getFrom(), entry.getTo());
            }
        }
        return result;
    }


    private static boolean shouldBeginsWithIndirectMappingApplied
            (FontMapEntry entry, String translated, String template)
    {
        if (entry.isBeginsWith() && template.startsWith(entry.getFrom())
                && (translated == null || translated.length() == 0))
        {
            return true;
        }
        return false;
    }

    private static boolean shouldPrecededByIndirectMappingApplied
            (FontMapEntry entry, String translated)
    {
        if (entry.getPrecededBy() != null && entry.getPrecededBy().length() > 0
                &&
                (translated != null &&
                        translated.endsWith(entry.getPrecededBy())))
        {
            return true;
        }
        return false;
    }

    private boolean isWordMapped(String word)
    {
        if (reverseTransliterate)
        {
            return entries.isInWord2(word);
        }
        return entries.isInWord1(word);
    }


}

