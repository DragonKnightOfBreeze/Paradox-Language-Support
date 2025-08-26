package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test

class StdlibFastExtensionsTest {
    @Test
    fun trimFast_basic() {
        Assert.assertEquals("abc", "///abc///".trimFast('/'))
        Assert.assertEquals("a/b/c", "/a/b/c/".trimFast('/'))
        Assert.assertEquals("abc", "abc".trimFast('/'))
        Assert.assertEquals("", "".trimFast('/'))
        Assert.assertEquals("", "////".trimFast('/'))
    }

    @Test
    fun splitFast_basic() {
        Assert.assertEquals(listOf("a","b","c"), "a|b|c".splitFast('|'))
        Assert.assertEquals(listOf("abc"), "abc".splitFast('|'))
        Assert.assertEquals(listOf("","b",""), "|b|".splitFast('|'))
    }

    @Test
    fun splitFast_ignoreCase_and_limit() {
        Assert.assertEquals(listOf("a","c"), "aBc".splitFast('b', ignoreCase = true))
        Assert.assertEquals(listOf("aB|c"), "aB|c".splitFast('|', limit = 1))
        Assert.assertEquals(listOf("a","B|c"), "a|B|c".splitFast('|', limit = 2))
    }
}
