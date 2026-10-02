package intellibitz.sted.event

import intellibitz.sted.fontmap.Converter

class TransliterateEvent(converter: Converter) : ThreadEvent(converter) {
    val converter: Converter
        get() = source as Converter
}
