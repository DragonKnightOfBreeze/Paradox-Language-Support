package icu.windea.pls.misc

import icu.windea.pls.core.*
import org.junit.*

class MiscTest {
    @Test
    fun test() {
        foo()
    }


    private fun foo() {
        val a = Thread.currentThread().stackTrace
        println(a)
        withRecursionGuard("a") {
            println()
        }
        println()
        println()
        withRecursionGuard("b") {
            println()
            println()
        }
    }
}
