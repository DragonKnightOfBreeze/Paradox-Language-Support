package icu.windea.pls.misc

import org.junit.Assert
import org.junit.Test

class LambdaTest {
    @Test
    fun test() {
        val h1 = processWithHashCode()
        val h2 = processWithHashCode()
        Assert.assertEquals(h1, h2)
    }

    private fun processWithHashCode(): Int {
        return process { 1 + 1 }
    }

    private fun process(action: () -> Unit): Int {
        action()
        return action.hashCode()
    }
}
