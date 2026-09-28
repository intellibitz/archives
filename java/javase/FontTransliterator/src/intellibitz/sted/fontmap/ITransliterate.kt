package intellibitz.sted.fontmap

interface ITransliterate {
    fun parseLine(input: String?): String?
    fun setReverseTransliterate(flag: Boolean)
    fun setHTMLAware(flag: Boolean)
    fun setEntries(entries: IEntries?)

    interface IEntries {
        fun getReverseMapping(wordToConvert: String?): IEntry?
        fun getDirectMapping(wordToConvert: String?): IEntry?
        fun isRuleFound(word: String?): List<FontMapEntry>?
        fun isInWord1(word: String?): Boolean
        fun isInWord2(word: String?): Boolean
    }

    interface IEntry {
        val from: String?
        val to: String?
    }
}
