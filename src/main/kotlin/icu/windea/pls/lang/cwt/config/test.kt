package icu.windea.pls.lang.cwt.config

import kotlin.reflect.*

class Foo(
    val a : String
) {
    var b:Any? = null
    
    val c get() = ::b.get()
}

inline fun <T : Any> KMutableProperty0<T>.get(): T {
    return this.get()
}