package icu.windea.pls

import icu.windea.pls.core.*
import org.junit.*

class ExtensionsTest {
	@Test
	fun matchesGlobFileNameTest(){
		Assert.assertTrue("abc".matchesGlobFileName("abc"))
		Assert.assertTrue("abc".matchesGlobFileName("*"))
		Assert.assertTrue("abc".matchesGlobFileName("ab."))
		Assert.assertTrue("abc".matchesGlobFileName("ab*"))
	}
	
	@Test
	fun matchesAntPathTest(){
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/foo/bar/name**", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/foo/bar/name", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/foo/bar/**", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/foo/**", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/**", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/foo/bar/nam.", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/foo/bar/na.e", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/foo/bar/na*.e", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/foo/bar/*", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/foo/bar/*a*e", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/foo/b*r/*a*e", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/foo/b*r/*a*e", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/*foo/*/name", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/*foo/*/n.me", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/*foo/**/n.me", false))
		Assert.assertTrue("/foo/bar/name".matchesAntPath("/*foo/**r/n.me", false))
		Assert.assertFalse("/foo/bar/name".matchesAntPath("/foo/*", false))
		Assert.assertFalse("/foo/bar/name".matchesAntPath("/*/name", false))
		Assert.assertFalse("/foo/bar/name".matchesAntPath("/foo/bar/na.", false))
		Assert.assertFalse("/foo/bar/name".matchesAntPath("/foo/bar/", false))
	}
	
	@Test
	fun escapeBlankTest(){
		Assert.assertEquals("abc", "abc".escapeBlank())
		Assert.assertEquals("abc&nbsp;", "abc ".escapeBlank())
		Assert.assertEquals("abc&nbsp;&nbsp;", "abc  ".escapeBlank())
		Assert.assertEquals("&nbsp;abc&nbsp;&nbsp;", " abc  ".escapeBlank())
		Assert.assertEquals("&nbsp;a&nbsp;bc&nbsp;&nbsp;", " a bc  ".escapeBlank())
	}
}