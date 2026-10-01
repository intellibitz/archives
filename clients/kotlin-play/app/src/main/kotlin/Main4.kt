package org.sushmu

import kotlin.reflect.KFunction
import kotlin.reflect.KFunction1

fun main() {
    pfn { it.unaryMinus() }
    pfn(::fn)
    pfn { fn(it) }
    val afn: KFunction1<Int, Any> = ::fn
    pfn(afn)
    pfn { afn.invoke(it) }
    val aafn: KFunction<Any> = afn
    pfn { aafn.call(it) }
    rpfn(aafn)
    val ll: (Int) -> Number = ::fn
    pfn(ll)
    pfn { ll(it) }
    val hash: (Any) -> Number = { it.hashCode() }
    pfn { hash(it) }
    val d: (Number) -> Number = { it.toDouble() }
    pfn(d)
    val id: (Number) -> Int = { it.toInt() }
    pfn(id)
    val str: (Any) -> String = { it.toString() }
    pfn { str(it) }
}

fun rpfn(f: KFunction<Any>) {
    println("value: " + f.call(42))
}

fun pfn(f: (Int) -> Any) {
    println("value: " + f(42))
}

fun fn(p: Int): Number {
    return p.unaryMinus()
}