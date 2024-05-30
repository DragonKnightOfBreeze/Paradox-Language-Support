package icu.windea.pls.lang

import org.junit.*

class PluginExtensionsTest {
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