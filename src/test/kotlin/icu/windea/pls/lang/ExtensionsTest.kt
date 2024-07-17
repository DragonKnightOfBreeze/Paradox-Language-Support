package icu.windea.pls.lang

import com.intellij.openapi.util.*
import org.junit.*

class ExtensionsTest {
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
        
        Assert.assertFalse("\$abc\\$".isFullParameterized())
        Assert.assertFalse("\\\$abc$".isParameterized())
        Assert.assertFalse("\\[[a]]".isParameterized())
        Assert.assertFalse("abc".isParameterized())
    }
    
    @Test
    fun isFullParameterizedTest() {
        Assert.assertTrue("\$abc$".isFullParameterized())
        Assert.assertFalse("aaa\$abc\$bbb".isFullParameterized())
        Assert.assertFalse("\$abc\\$".isFullParameterized())
        Assert.assertFalse("\\\$abc$".isParameterized())
    }
    
    @Test
    fun getParameterRangesTest() {
        Assert.assertEquals(listOf(TextRange.create(0, 5)), "\$abc$".getParameterRanges())
        Assert.assertEquals(listOf(TextRange.create(3, 8)), "aaa\$abc\$bbb".getParameterRanges())
        Assert.assertEquals(listOf(TextRange.create(0, 5)), "[[a]]".getParameterRanges())
        Assert.assertEquals(listOf(TextRange.create(3, 8)), "aaa[[a]]bbb".getParameterRanges())
        
        Assert.assertEquals(listOf(TextRange.create(1, 4), TextRange.create(5, 13)), "a\$a\$a[[a]\$b$]bbb".getParameterRanges())
    }
    
    @Test
    fun toRegexWhenIsParameterizedTest() {
        val r1 = "a\$b\$c".toRegexWhenIsParameterized()
        Assert.assertTrue(r1.matches("ac"))
        Assert.assertTrue(r1.matches("abc"))
        Assert.assertTrue(r1.matches("abbc"))
        
        val r2 = "a\$b\$c[[d]e]".toRegexWhenIsParameterized()
        Assert.assertTrue(r2.matches("abc"))
        Assert.assertTrue(r2.matches("abce"))
        Assert.assertFalse(r2.matches("abcd"))
        
        val r3 = "a\$b\$c[[d]\$e\$]".toRegexWhenIsParameterized()
        Assert.assertTrue(r3.matches("abc"))
        Assert.assertTrue(r3.matches("abce"))
        Assert.assertTrue(r3.matches("abcd"))
        
        val r4 = "a\$b\$c[[d]\$e\$f]".toRegexWhenIsParameterized()
        Assert.assertTrue(r4.matches("abcf"))
        Assert.assertTrue(r4.matches("abcef"))
        Assert.assertTrue(r4.matches("abcdf"))
    }
}
