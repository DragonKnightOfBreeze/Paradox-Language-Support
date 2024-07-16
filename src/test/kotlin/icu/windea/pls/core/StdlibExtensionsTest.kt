package icu.windea.pls.core

import org.junit.*

class StdlibExtensionsTest {
	@Test
	fun matchesGlobPatternTest(){
		Assert.assertTrue("abc".matchesPattern("abc"))
		Assert.assertTrue("abc".matchesPattern("*"))
		Assert.assertTrue("abc".matchesPattern("ab?"))
		Assert.assertTrue("abc".matchesPattern("ab*"))
		Assert.assertTrue("abc".matchesPattern("a?c"))
		Assert.assertFalse("ab".matchesPattern("a?c"))
		Assert.assertFalse("abc".matchesPattern("a?"))
		Assert.assertTrue("abc".matchesPattern("a*c"))
		Assert.assertFalse("abc".matchesPattern("a*b"))
	}
	
	@Test
	fun matchesAntPatternTest(){
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/foo/bar/name**", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("foo/bar/name**", false))
		Assert.assertTrue("foo/bar/name".matchesAntPattern("/foo/bar/name**", false))
		Assert.assertTrue("foo/bar/name".matchesAntPattern("foo/bar/name**", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/foo/bar/name", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/foo/bar/**", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/foo/**", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/foo/**/name", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/foo/**/bar/name", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/**", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/foo/bar/nam?", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/foo/bar/na?e", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/foo/bar/na*?e", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/foo/bar/*", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/foo/bar/*a*e", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/foo/b*r/*a*e", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/foo/b*r/*a*e", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/*foo/*/name", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/*foo/*/n?me", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/*foo/**/n?me", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPattern("/*foo/**r/n?me", false))
		Assert.assertFalse("/foo/bar/name".matchesAntPattern("/foo/*", false))
		Assert.assertFalse("/foo/bar/name".matchesAntPattern("/*/name", false))
		Assert.assertFalse("/foo/bar/name".matchesAntPattern("/foo/bar/na?", false))
		Assert.assertFalse("/foo/bar/name".matchesAntPattern("/foo/bar/", false))

		Assert.assertTrue("enums/enum[e]".matchesAntPattern("enums/enum[?]", false))
		Assert.assertTrue("enums/enum[a".matchesAntPattern("enums/enum[?", false))
		Assert.assertFalse("enums/enum[f".matchesAntPattern("enums/enum[?]", false))
		Assert.assertTrue("enums/enum[e]".matchesAntPattern("enums/enum[*]", false))
		Assert.assertFalse("enums/enum[e".matchesAntPattern("enums/enum[*]", false))
		Assert.assertFalse("enums/enum123".matchesAntPattern("enums/enum[*]", false))
	}
	
	@Test
	fun escapeBlankTest(){
		Assert.assertEquals("abc", "abc".escapeBlank())
		Assert.assertEquals("abc&nbsp;", "abc ".escapeBlank())
		Assert.assertEquals("abc&nbsp;&nbsp;", "abc  ".escapeBlank())
		Assert.assertEquals("&nbsp;abc&nbsp;&nbsp;", " abc  ".escapeBlank())
		Assert.assertEquals("&nbsp;a&nbsp;bc&nbsp;&nbsp;", " a bc  ".escapeBlank())
	}
	
	@Test
	fun quoteAndUnquoteTest() {
		Assert.assertEquals(""""#####\\\" \" \\\\ ai_chance = {}"""", """#####\" " \\ ai_chance = {}""".quote())
		
		Assert.assertEquals("\"abc\\\"\"", "abc\"".quote())
		
		Assert.assertEquals("\"abc\"", "abc".quote())
		Assert.assertEquals("\"abc\"", "\"abc\"".quote())
		
		Assert.assertEquals("""" abc\"abc """", """ abc"abc """.quote())
		Assert.assertEquals("""" abc\\\"abc """", """ abc\"abc """.quote())
		Assert.assertEquals(""" abc"abc """, """" abc\"abc """".unquote())
		Assert.assertEquals(""" abc\"abc """, """" abc\\\"abc """".unquote())
		
		Assert.assertEquals("abc", "abc".unquote())
		Assert.assertEquals("ab\"c", "ab\\\"c".unquote())
		Assert.assertEquals("abc\"", "abc\\\"".unquote())
		Assert.assertEquals("\"abc", "\\\"abc".unquote())
		Assert.assertEquals("\"abc\"", "\\\"abc\\\"".unquote())
		Assert.assertEquals("abc", "\"abc\"".unquote())
		Assert.assertEquals("abc", "\"abc".unquote())
		Assert.assertEquals("abc", "abc\"".unquote())
		Assert.assertEquals("abc abc", "abc abc".unquote())
		Assert.assertEquals("abc abc", "\"abc abc\"".unquote())
		Assert.assertEquals("abc abc", "\"abc abc".unquote())
		Assert.assertEquals("abc abc", "abc abc\"".unquote())
	}
	
	@Test
	fun isQuotedTest() {
		Assert.assertFalse("123".isRightQuoted())
		Assert.assertTrue("123\"".isRightQuoted())
		Assert.assertFalse("123\\\"".isRightQuoted())
		Assert.assertTrue("123\\\\\"".isRightQuoted())
		Assert.assertTrue("\\\\\"".isRightQuoted())
	}
    
    @Test
    fun isEscapedCharAt(){
        Assert.assertFalse("abcd".isEscapedCharAt(3))
        Assert.assertTrue("ab\\d".isEscapedCharAt(3))
        Assert.assertFalse("a\\\\d".isEscapedCharAt(3))
        Assert.assertTrue("\\\\\\d".isEscapedCharAt(3))
    }
}
