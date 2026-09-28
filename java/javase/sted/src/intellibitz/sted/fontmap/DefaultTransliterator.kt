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
 * $Id:DefaultTransliterator.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/fontmap/DefaultTransliterator.kt $
 */

package intellibitz.sted.fontmap

import intellibitz.sted.util.Resources
import java.util.StringTokenizer

class DefaultTransliterator : ITransliterate {
    private var reverseTransliterate = false
    private var isHTMLAware = true
    private var isParseMode = true
    private var entries: ITransliterate.IEntries? = null

    private var converted = 0
    private var prevWord: String? = null

    override fun setEntries(entries: ITransliterate.IEntries?) {
        this.entries = entries
    }

    override fun setReverseTransliterate(flag: Boolean) {
        reverseTransliterate = flag
    }

    override fun setHTMLAware(flag: Boolean) {
        isHTMLAware = flag
    }

    override fun parseLine(input: String?): String {
        if (input == null) return ""
        val output = StringBuilder()
        val stringTokenizer = StringTokenizer(input, " ", true)
        while (stringTokenizer.hasMoreTokens()) {
            val word = stringTokenizer.nextToken()
            val st = StringTokenizer(
                word,
                Resources.HTML_TAG_START + Resources.HTML_TAG_END +
                Resources.HTML_TAG_START_ESCAPE + Resources.HTML_TAG_END_ESCAPE +
                Resources.SPACE,
                true
            )
            while (st.hasMoreTokens()) {
                val currWord = st.nextToken()
                parseWord(currWord, prevWord, output)
                prevWord = currWord
            }
        }
        return output.toString()
    }

    private fun parseWord(word: String, prevWord: String?, output: StringBuilder) {
        if (Resources.SPACE == word) {
            output.append(word)
            return
        }
        if ((Resources.HTML_TAG_START == word || Resources.HTML_TAG_START_ESCAPE == word) && isHTMLAware) {
            isParseMode = false
            output.append(word)
            return
        } else if ((Resources.HTML_TAG_END == word || Resources.HTML_TAG_END_ESCAPE == word) && isHTMLAware) {
            isParseMode = true
            if (Resources.HTML_TAG_END_ESCAPE == word && "lt" == prevWord) {
                isParseMode = false
            }
            output.append(word)
            return
        }
        if (!(Resources.HTML_TAG_END == word || Resources.HTML_TAG_END_ESCAPE == word) && !isParseMode) {
            output.append(word)
            return
        }
        converted = 0
        convertWord(word, output, word, word.length, Resources.EMPTY_STRING, Resources.EMPTY_STRING, word)
    }

    private fun convertWord(
        word: String, output: StringBuilder, chopped: String,
        wordLen: Int, translatedArg: String, leftoverArg: String,
        original: String
    ) {
        var translated = translatedArg
        var leftover = leftoverArg
        
        if (converted == wordLen) {
            return
        }
        if (translate(word, output, translated, leftover, original)) {
            converted += word.length
        } else {
            var remaining = word
            if (word.length > 1) {
                remaining = word.substring(0, word.length - 1)
                leftover = word.substring(word.length - 1) + leftover
            }
            convertWord(remaining, output, chopped, wordLen, translated, leftover, original)
            translated += remaining
            val remaining2 = chopped.substring(remaining.length)
            leftover = Resources.EMPTY_STRING
            convertWord(remaining2, output, remaining2, wordLen, translated, leftover, original)
        }
    }

    private fun translate(
        word: String, output: StringBuilder,
        translated: String,
        leftover: String, original: String
    ): Boolean {
        val result: CharArray? = if (isParseMode) {
            translateWord(word, translated, leftover, original)
        } else {
            word.toCharArray()
        }
        if (result != null) {
            output.append(result)
            return true
        }
        return false
    }

    private fun translateWord(
        word: String, translated: String,
        leftover: String,
        original: String
    ): CharArray? {
        val wordToConvert = applyIndirectMappingIfAny(word, translated, leftover, original)
        var chars: CharArray? = null
        if (isWordMapped(wordToConvert)) {
            if (reverseTransliterate) {
                val entry = entries?.getReverseMapping(wordToConvert)
                chars = entry?.from?.toCharArray() ?: wordToConvert.toCharArray()
            } else {
                val entry = entries?.getDirectMapping(wordToConvert)
                chars = entry?.to?.toCharArray() ?: wordToConvert.toCharArray()
            }
        }
        if (chars == null && wordToConvert.length == 1) {
            chars = charArrayOf(wordToConvert[0])
        }
        return chars
    }

    private fun applyIndirectMappingIfAny(
        word: String, translated: String,
        leftover: String, template: String
    ): String {
        var result = word
        var list = entries?.isRuleFound(word) ?: emptyList()
        if (list.isNotEmpty()) {
            for (entry in list) {
                result = indirectMap(entry, result, translated, leftover, template)
            }
        } else if (entries?.isInWord1(word) == true) {
            var len = word.length
            while (len > 0) {
                val nword = result.substring(0, len--)
                list = entries?.isRuleFound(nword) ?: emptyList()
                if (list.isNotEmpty()) {
                    result = word
                    for (entry in list) {
                        if (shouldBeginsWithIndirectMappingApplied(entry, translated, template) || 
                            shouldPrecededByIndirectMappingApplied(entry, translated)) {
                            result = indirectMap(entry, result, translated, leftover, template)
                        }
                    }
                }
            }
        }
        return result
    }

    private fun isWordMapped(word: String): Boolean {
        return if (reverseTransliterate) {
            entries?.isInWord2(word) == true
        } else {
            entries?.isInWord1(word) == true
        }
    }

    companion object {
        private fun indirectMap(
            entry: FontMapEntry, word: String,
            translated: String?, leftover: String,
            template: String
        ): String {
            var result = word
            if (shouldBeginsWithIndirectMappingApplied(entry, translated, template)) {
                if (!template.startsWith(entry.to)) {
                    result = word.replaceFirst(entry.from.toRegex(), entry.to)
                }
            }
            if (entry.endsWith && translated != null && template.length - translated.length == 1) {
                if (!template.endsWith(entry.to)) {
                    result = word.replaceFirst(entry.from.toRegex(), entry.to)
                }
            }
            if (!entry.followedBy.isNullOrEmpty() && leftover.startsWith(entry.followedBy!!)) {
                if (word == entry.from || word.length >= entry.to.length && word.substring(word.indexOf(entry.from), word.indexOf(entry.from) + entry.to.length) != entry.to) {
                    result = word.replaceFirst(entry.from.toRegex(), entry.to)
                }
            }
            if (shouldPrecededByIndirectMappingApplied(entry, translated)) {
                if (word == entry.from || word.length >= entry.to.length && word.substring(word.indexOf(entry.from), word.indexOf(entry.from) + entry.to.length) != entry.to) {
                    result = word.replaceFirst(entry.from.toRegex(), entry.to)
                }
            }
            return result
        }

        private fun shouldBeginsWithIndirectMappingApplied(
            entry: FontMapEntry, translated: String?, template: String
        ): Boolean {
            return entry.beginsWith && template.startsWith(entry.from) && (translated.isNullOrEmpty())
        }

        private fun shouldPrecededByIndirectMappingApplied(
            entry: FontMapEntry, translated: String?
        ): Boolean {
            return !entry.precededBy.isNullOrEmpty() && translated != null && translated.endsWith(entry.precededBy!!)
        }
    }
}
