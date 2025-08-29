package icu.windea.pls.misc

import icu.windea.pls.core.withRecursionGuard
import org.junit.Test

class MiscTest {
    @Test
    fun test() {
        foo()
    }


    private fun foo() {
        val a = Thread.currentThread().stackTrace
        println(a)
        withRecursionGuard {
            println()
        }
        println()
        println()
        withRecursionGuard {
            println()
            println()
        }
    }
}
