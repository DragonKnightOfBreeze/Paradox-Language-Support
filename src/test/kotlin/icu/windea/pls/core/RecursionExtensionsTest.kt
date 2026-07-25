package icu.windea.pls.core

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RecursionExtensionsTest {
    @Test
    fun testAnonymousRecursionGuard() {
        val r1 = methodA()
        val r2 = methodA()
        val r3 = methodB()
        assertEquals(r1, r2)
        assertNotEquals(r1, r3)
    }

    private fun methodA(): String? {
        return withRecursionGuard({}.javaClass.name) { name }
    }

    private fun methodB() : String?{
        return withRecursionGuard({}.javaClass.name) { name }
    }
}
