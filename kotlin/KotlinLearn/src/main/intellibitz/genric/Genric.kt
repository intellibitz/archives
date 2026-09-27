package genric

import java.util.*

fun main() {
    println(listOf(1, 2, 3).penultimate)
    println(oneHalf(33))
//    println(oneHalf("12")
    println(ensureTrailingPeriod(hello))
    println(hello)
    println(ip.process(2))
    println(sp.process(null))
    println(isA<Int>(2))
    printContents(listOf(1, 2, 3))
    printContents(listOf('1', '2', 3))
//    unknown elements projection can be accessed, not modified
    println(unknownElements.first())
    printFirst(unknownElements)
}

inline fun <reified T> isA(value: Any) = value is T

val list: MutableList<Any?> = mutableListOf('a', 1, "qwe")
val chars = mutableListOf('a', 'b', 'c')

// Note: MutableList<*> isn’t the same as MutableList<Any?>
val unknownElements: MutableList<*> = if (Random().nextBoolean()) list else chars

//fun<T> printFirst(list: List<T>) {
// you only read the data and you don’t care about its specific type
fun printFirst(list: List<*>) {
    if (list.isNotEmpty()) {
        println(list.first())
    }
}

fun printContents(list: List<Any>) {
    println(list.joinToString())
}

val ip: Processor<Int> = Processor()
val sp = Processor<String?>()

class Processor<T> {
    fun process(value: T) {
        value?.hashCode()
    }
}

//generic extension property
val <T> List<T>.penultimate: T
    get() = this[size - 2]
//NOTE: generic non extension property, generic regular properties are not supported
//val <T> x: T = TODO()

fun <T : Number> oneHalf(value: T): Double {
//    invokes methods defined in upper bound type (Number in this case)
    return value.toDouble() / 2
}

val hello = StringBuilder("Hello World")

// specifies multiple constraints on type parameter (Note: CharSequence, Appendable)
fun <T> ensureTrailingPeriod(seq: T) where T : CharSequence, T : Appendable {
    if (!seq.endsWith(".")) seq.append(".")
}

/*
Kotlin’s generics are fairly similar to those in Java: you declare a generic function or
class in the same way.
As in Java, type arguments for generic types only exist at compile time.
You can’t use types with type arguments together with the is operator, because type
arguments are erased at runtime.
Type parameters of inline functions can be marked as reified, which allows you to use
them at runtime to perform is checks and obtain java.lang.Class instances.
Variance is a way to specify whether the type parameter of a type can be substituted for
its subclass or superclass.
You can declare a class as covariant on a type parameter if the parameter used only in
out positions.
The opposite is true for contravariant cases: you can declare a class as contravariant on a
type parameter if it’s used only in in positions.
The read-only interface List in Kotlin is declared as covariant, which means
List<String> is a subtype of List<Any>.
The function interface is declared as contravariant on its first type parameter and
covariant on its second, which makes (Animal)->Int a subtype of (Cat)->Number.
Kotlin lets you specify variance both for a generic class as a whole (declaration-site
variance) and for a specific use of a generic type (use-site variance).
The star-projection syntax can be used when the exact type arguments are unknown or
unimportant.
 */