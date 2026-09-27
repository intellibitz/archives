package org.sushmu

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun main() {
    println(1)
    val a = suspendCoroutine {
        it.resume(1)
    }
    println(a)
    println(2)
}