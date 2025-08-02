package icu.windea.pls

import com.intellij.openapi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import org.junit.*
import org.junit.Assert.*

class ExtensionsTest2 {
    @Test
    fun isParameterAwareIdentifierTest() {
        assertTrue("\$abc$".isParameterAwareIdentifier())
        assertTrue("aaa\$abc\$bbb".isParameterAwareIdentifier())
        assertTrue("[[a]]".isParameterAwareIdentifier())
        assertTrue("aaa[[a]]bbb".isParameterAwareIdentifier())
    }

    @Test
    fun isParameterizedTest() {
        assertTrue("\$abc$".isParameterized())
        assertTrue("aaa\$abc\$bbb".isParameterized())
        assertTrue("[[a]]".isParameterized())
        assertTrue("aaa[[a]]bbb".isParameterized())

        assertTrue("\$abc\\$".isParameterized())
        assertFalse("\\\$abc$".isParameterized())
        assertFalse("\\[[a]]".isParameterized())
        assertFalse("abc".isParameterized())
    }

    @Test
    fun isFullParameterizedTest() {
        assertTrue("\$abc$".isParameterized(full = true))
        assertFalse("aaa\$abc\$bbb".isParameterized(full = true))
        assertFalse("\$abc\\$".isParameterized(full = true))
        assertFalse("\\\$abc$".isParameterized(full = true))
        assertFalse("\$abc\$def\$gh$".isParameterized(full = true))
    }

    @Test
    fun getParameterRangesTest() {
        assertEquals(listOf(TextRange.create(0, 5)), ParadoxExpressionManager.getParameterRanges("\$abc$"))
        assertEquals(listOf(TextRange.create(3, 8)), ParadoxExpressionManager.getParameterRanges("aaa\$abc\$bbb"))
        assertEquals(listOf(TextRange.create(0, 5)), ParadoxExpressionManager.getParameterRanges("[[a]]"))
        assertEquals(listOf(TextRange.create(3, 8)), ParadoxExpressionManager.getParameterRanges("aaa[[a]]bbb"))

        assertEquals(listOf(TextRange.create(1, 4), TextRange.create(5, 13)), ParadoxExpressionManager.getParameterRanges("a\$a\$a[[a]\$b$]bbb"))
    }

    @Test
    fun toRegexWhenIsParameterizedTest() {
        val r1 = ParadoxExpressionManager.toRegex("a\$b\$c")
        assertTrue(r1.matches("ac"))
        assertTrue(r1.matches("abc"))
        assertTrue(r1.matches("abbc"))

        val r2 = ParadoxExpressionManager.toRegex("a\$b\$c[[d]e]")
        assertTrue(r2.matches("abc"))
        assertTrue(r2.matches("abce"))
        assertFalse(r2.matches("abcd"))

        val r3 = ParadoxExpressionManager.toRegex("a\$b\$c[[d]\$e$]")
        assertTrue(r3.matches("abc"))
        assertTrue(r3.matches("abce"))
        assertTrue(r3.matches("abcd"))

        val r4 = ParadoxExpressionManager.toRegex("a\$b\$c[[d]\$e\$f]")
        assertTrue(r4.matches("abcf"))
        assertTrue(r4.matches("abcef"))
        assertTrue(r4.matches("abcdf"))
    }
}
