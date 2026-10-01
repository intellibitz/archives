package org.sushmu

import kotlin.reflect.full.memberProperties

fun main() {
    val granny = Person(
        "Esmeralda",
        "Weatherwax", 0, true
    )
    displayPropertiesAsList(granny)
}

fun displayPropertiesAsList(value: Any) {
    value::class.memberProperties
        .sortedBy { it.name }
        .map { p -> " * ${p.name}: ${p.call(value)}" }
        .forEach(::println)
}

class Person(
    val name: String,
    val surname: String,
    val children: Int,
    val female: Boolean,
)
