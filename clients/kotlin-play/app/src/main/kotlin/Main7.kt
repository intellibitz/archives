package org.sushmu

import kotlinx.coroutines.*

// import kotlin.reflect
/**
 * playground project
 */

val a = fun() {}
val b = {}
val c = object {}

class d

val e = d()

interface f<T>

val g = object : f<Int> {}
fun h() {}
val i = ::h

abstract class j : () -> Unit

val k = {}.invoke()
val l = i()
fun m() = {}
val n = m()
val o = n()

public interface A<T>
open class B : Any()
private data class C(val d: Int, val e: String = "one") : B(), A<B>
internal data class De(val a: Char = '\t')
data class E(val a: Byte = 0B01)

val F = Any()
var G = object {}

class H
object I
data object J
sealed interface K
enum class L

val seq = sequence {
    yield(1)
}

suspend fun sus() {
    repeat(1_00_000) {}
}

fun main() {
    runBlocking {
        coroutineScope {
            launch {
                sus();
            }

        }
    }
    val iter = seq.iterator();
    println(iter.next())
    for (num in seq) println(num)
    main2()
}

private fun main2() {
    generateSequence(1) { it + 1 }
        .map { it * 2 }
        .take(10)
        .forEach { print("$it") }

    println(J)
    println(listOf(C(1)).first().e)
    println(fun() {})
    val (d: Int, e: String) = C(1)
    println("Hello, world!!!" + ab(3, 4, 5) + nest() + d + e)
    for (i in 1..3) print(i)
    println(B())
    println(De())
}

fun ab(vararg i: Int): Int {
    var a = 1
    for (b in i) a = a * b
    return a
}

fun nest() {
    fun n1() {
        println("n1")
    }
    n1()
}
