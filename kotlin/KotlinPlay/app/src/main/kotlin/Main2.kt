package org.sushmu

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

fun main() {

    val iter = sequence {
        yield(1)
    }.iterator()
    println(iter.next())

    val f = flow {
        emit(1)
    }
    runBlocking {
        with(f) {
            collect(collector = {
                println(it)
            })
        }

    }

}