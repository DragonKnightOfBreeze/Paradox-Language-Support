package icu.windea.pls

import org.junit.*

class ExtensionsTest {
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
}