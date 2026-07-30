package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test

class StdlibFastExtensionsTest {
    @Test
    fun equalsFast_basic_test() {
        Assert.assertTrue(null.equalsFast(null))
        Assert.assertFalse(null.equalsFast(""))
        Assert.assertFalse("".equalsFast(null))
        Assert.assertTrue("".equalsFast(""))
        Assert.assertTrue("abc".equalsFast("abc"))
        Assert.assertTrue("abc".equalsFast(String("abc".toCharArray())))
        Assert.assertFalse("".equalsFast("abc"))
        Assert.assertFalse("".equalsFast(String("abc".toCharArray())))

        Assert.assertTrue("hello world".equalsFast("hello world"))
        Assert.assertTrue("hello world".equalsFast("Hello World", ignoreCase = true))
        Assert.assertFalse("hello world".equalsFast("hello world!"))
        Assert.assertFalse("hello world".equalsFast("Hello World!", ignoreCase = true))

        Assert.assertTrue("prompt: 你好，世界".equalsFast("prompt: 你好，世界"))
        Assert.assertTrue("prompt: 你好，世界".equalsFast("PROMPT: 你好，世界", ignoreCase = true))
        Assert.assertFalse("prompt: 你好，世界".equalsFast("prompt: 你好，世界！"))
        Assert.assertFalse("prompt: 你好，世界".equalsFast("PROMPT: 你好，世界！", ignoreCase = true))
    }

    @Test
    fun trimFast_basic_test() {
        Assert.assertEquals("abc", "///abc///".trimFast('/'))
        Assert.assertEquals("a/b/c", "/a/b/c/".trimFast('/'))
        Assert.assertEquals("abc", "abc".trimFast('/'))
        Assert.assertEquals("", "".trimFast('/'))
        Assert.assertEquals("", "////".trimFast('/'))
    }

    @Test
    fun splitFast_basic_test() {
        Assert.assertEquals(listOf("a", "b", "c"), "a|b|c".splitFast('|'))
        Assert.assertEquals(listOf("abc"), "abc".splitFast('|'))
        Assert.assertEquals(listOf("", "b", ""), "|b|".splitFast('|'))
    }

    @Test
    fun splitFast_ignoreCase_and_limit_test() {
        Assert.assertEquals(listOf("a", "c"), "aBc".splitFast('b', ignoreCase = true))
        Assert.assertEquals(listOf("aB|c"), "aB|c".splitFast('|', limit = 1))
        Assert.assertEquals(listOf("a", "B|c"), "a|B|c".splitFast('|', limit = 2))
    }
}
