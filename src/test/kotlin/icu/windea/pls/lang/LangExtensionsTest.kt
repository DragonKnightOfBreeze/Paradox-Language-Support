package icu.windea.pls.lang

import org.junit.Assert
import org.junit.Test

class LangExtensionsTest {
    @Test
    fun isIdentifierChar_test() {
        Assert.assertTrue('a'.isIdentifierChar())
        Assert.assertTrue('1'.isIdentifierChar())
        Assert.assertTrue('_'.isIdentifierChar())
        Assert.assertTrue('$'.isIdentifierChar()) // true
        Assert.assertTrue('.'.isIdentifierChar("."))
    }

    @Test
    fun isIdentifier_test() {
        Assert.assertFalse("".isIdentifier())
        Assert.assertTrue("a".isIdentifier())
        Assert.assertTrue("1".isIdentifier())
        Assert.assertTrue("_".isIdentifier())
        Assert.assertTrue("$".isIdentifier()) // true
        Assert.assertTrue(".".isIdentifier("."))
    }

    @Test
    fun isParameterAwareIdentifier_test() {
        Assert.assertFalse("".isParameterAwareIdentifier())
        Assert.assertTrue("\$abc$".isParameterAwareIdentifier())
        Assert.assertTrue("aaa\$abc\$bbb".isParameterAwareIdentifier())
        Assert.assertTrue("[[a]]".isParameterAwareIdentifier())
        Assert.assertTrue("aaa[[a]]bbb".isParameterAwareIdentifier())
    }
}
