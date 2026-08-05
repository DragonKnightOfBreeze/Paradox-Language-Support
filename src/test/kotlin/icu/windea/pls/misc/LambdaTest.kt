package icu.windea.pls.misc

import org.junit.Assert.*
import org.junit.Test

class LambdaTest {
    @Test
    fun testBasic() {
        val r1 = processBasicA()
        val r2 = processBasicA()
        val r3 = processBasicB()
        assertEquals(r1, r2)
        assertNotEquals(r1, r3)
    }

    // region Helpers
    private fun processBasicA(): String {
        return processBasic { 1 + 1 }
    }

    private fun processBasicB(): String {
        return processBasic { 1 + 1 }
    }

    private fun processBasic(action: () -> Unit): String {
        action()
        return action.javaClass.name
    }
    // endregion

    @Test
    fun testNotDefault() {
        val r1 = processNotDefaultA()
        val r2 = processNotDefaultA()
        val r3 = processNotDefaultB()
        assertEquals(r1, r2)
        assertNotEquals(r1, r3)
    }

    // region Helpers
    private fun processNotDefaultA(): String {
        return processNotDefault({ }) { println("a") }
    }

    private fun processNotDefaultB(): String {
        return processNotDefault({ }) { println("b") }
    }

    private fun processNotDefault(name: () -> Unit, action: () -> Unit): String {
        action()
        return name.javaClass.name
    }
    // endregion

    @Test
    fun testDefault() {
        val r1 = processDefault()
        val r2 = processDefault()
        assertEquals(r1, r2)
    }

    // region Helpers
    private fun processDefault(name: () -> Unit = {}): String {
        return name.javaClass.name
    }
    // endregion
}
