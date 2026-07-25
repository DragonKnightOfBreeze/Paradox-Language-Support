package icu.windea.pls.lang

import org.junit.Assert
import org.junit.Test

class LangExtensionsTest {
    @Test
    fun isIdentifierCharTest() {
        Assert.assertTrue('a'.isIdentifierChar())
        Assert.assertTrue('1'.isIdentifierChar())
        Assert.assertTrue('_'.isIdentifierChar())
        Assert.assertTrue('$'.isIdentifierChar()) // true
        Assert.assertTrue('.'.isIdentifierChar("."))
    }

    @Test
    fun isIdentifierTest() {
        Assert.assertFalse("".isIdentifier())
        Assert.assertTrue("a".isIdentifier())
        Assert.assertTrue("1".isIdentifier())
        Assert.assertTrue("_".isIdentifier())
        Assert.assertTrue("$".isIdentifier()) // true
        Assert.assertTrue(".".isIdentifier("."))
    }

    @Test
    fun isParameterAwareIdentifierTest() {
        Assert.assertFalse("".isParameterAwareIdentifier())
        Assert.assertTrue("\$abc$".isParameterAwareIdentifier())
        Assert.assertTrue("aaa\$abc\$bbb".isParameterAwareIdentifier())
        Assert.assertTrue("[[a]]".isParameterAwareIdentifier())
        Assert.assertTrue("aaa[[a]]bbb".isParameterAwareIdentifier())
    }

    @Test
    fun isParameterizedTest() {
        Assert.assertFalse("".isParameterized())
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
        Assert.assertFalse("".isParameterized(full = true))
        Assert.assertTrue("\$abc$".isParameterized(full = true))
        Assert.assertFalse("aaa\$abc\$bbb".isParameterized(full = true))
        Assert.assertFalse("\$abc\\$".isParameterized(full = true))
        Assert.assertFalse("\\\$abc$".isParameterized(full = true))
        Assert.assertFalse("\$abc\$def\$gh$".isParameterized(full = true))
    }
}
