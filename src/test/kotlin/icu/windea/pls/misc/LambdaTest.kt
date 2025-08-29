package icu.windea.pls.misc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LambdaTest {
    @Test
    fun test() {
        val h1 = processWithHashCode()
        val h2 = processWithHashCode()
        val h3 = processWithHashCode1()
        assertEquals(h1, h2)
        assertNotEquals(h1, h3)
    }

    private fun processWithHashCode(): Int {
        return process { 1 + 1 }
    }

    private fun processWithHashCode1(): Int {
        return process { 1 + 1 }
    }

    private fun process(action: () -> Unit): Int {
        action()
        return action.hashCode()
    }
}
