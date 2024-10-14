package icu.windea.pls.test

import com.intellij.openapi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import org.junit.*

class ExtensionsTest2 {
    @Test
    fun isParameterAwareIdentifierTest() {
        Assert.assertTrue("\$abc$".isParameterAwareIdentifier())
        Assert.assertTrue("aaa\$abc\$bbb".isParameterAwareIdentifier())
        Assert.assertTrue("[[a]]".isParameterAwareIdentifier())
        Assert.assertTrue("aaa[[a]]bbb".isParameterAwareIdentifier())
    }

    @Test
    fun isParameterizedTest() {
        Assert.assertTrue("\$abc$".isParameterized())
        Assert.assertTrue("aaa\$abc\$bbb".isParameterized())
        Assert.assertTrue("[[a]]".isParameterized())
        Assert.assertTrue("aaa[[a]]bbb".isParameterized())

        Assert.assertTrue("\$abc\\$".isParameterized())
        Assert.assertFalse("\\\$abc$".isParameterized())
        Assert.assertFalse("\\[[a]]".isParameterized())
        Assert.assertFalse("abc".isParameterized())
    }

    @Test
    fun isFullParameterizedTest() {
        Assert.assertTrue("\$abc$".isFullParameterized())
        Assert.assertFalse("aaa\$abc\$bbb".isFullParameterized())
        Assert.assertFalse("\$abc\\$".isFullParameterized())
        Assert.assertFalse("\\\$abc$".isFullParameterized())
        Assert.assertFalse("\$abc\$def\$gh\$".isFullParameterized())
    }

    @Test
    fun getParameterRangesTest() {
        Assert.assertEquals(listOf(TextRange.create(0, 5)), ParadoxExpressionManager.getParameterRanges("\$abc$"))
        Assert.assertEquals(listOf(TextRange.create(3, 8)), ParadoxExpressionManager.getParameterRanges("aaa\$abc\$bbb"))
        Assert.assertEquals(listOf(TextRange.create(0, 5)), ParadoxExpressionManager.getParameterRanges("[[a]]"))
        Assert.assertEquals(listOf(TextRange.create(3, 8)), ParadoxExpressionManager.getParameterRanges("aaa[[a]]bbb"))

        Assert.assertEquals(listOf(TextRange.create(1, 4), TextRange.create(5, 13)), ParadoxExpressionManager.getParameterRanges("a\$a\$a[[a]\$b$]bbb"))
    }

    @Test
    fun toRegexWhenIsParameterizedTest() {
        val r1 = ParadoxExpressionManager.toRegex("a\$b\$c")
        Assert.assertTrue(r1.matches("ac"))
        Assert.assertTrue(r1.matches("abc"))
        Assert.assertTrue(r1.matches("abbc"))

        val r2 = ParadoxExpressionManager.toRegex("a\$b\$c[[d]e]")
        Assert.assertTrue(r2.matches("abc"))
        Assert.assertTrue(r2.matches("abce"))
        Assert.assertFalse(r2.matches("abcd"))

        val r3 = ParadoxExpressionManager.toRegex("a\$b\$c[[d]\$e\$]")
        Assert.assertTrue(r3.matches("abc"))
        Assert.assertTrue(r3.matches("abce"))
        Assert.assertTrue(r3.matches("abcd"))

        val r4 = ParadoxExpressionManager.toRegex("a\$b\$c[[d]\$e\$f]")
        Assert.assertTrue(r4.matches("abcf"))
        Assert.assertTrue(r4.matches("abcef"))
        Assert.assertTrue(r4.matches("abcdf"))
    }
}
