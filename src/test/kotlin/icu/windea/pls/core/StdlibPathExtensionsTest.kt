package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test

class StdlibPathExtensionsTest {
    @Test
    fun matchesPath_basic_and_strict() {
        // equal
        Assert.assertTrue("/a/b".matchesPath("/a/b", acceptSelf = true))
        Assert.assertFalse("/a/b".matchesPath("/a/b", acceptSelf = false))

        // parent-child
        Assert.assertTrue("/a".matchesPath("/a/b"))
        Assert.assertTrue("/a/b".matchesPath("/a/b/c"))
        Assert.assertFalse("/a/b/c".matchesPath("/a/b"))

        // strict: only direct parent
        Assert.assertTrue("/a".matchesPath("/a/b", strict = true))
        Assert.assertFalse("/a".matchesPath("/a/b/c", strict = true))
    }

    @Test
    fun matchesPath_trim() {
        // when trim=true, only the receiver is trimmed
        Assert.assertTrue("a/b/".matchesPath("a/b/c", trim = true))
        Assert.assertTrue("a/b".matchesPath("a/b/c", trim = false))
        Assert.assertFalse("a/b/".matchesPath("a-b/c", trim = true))
    }

    @Test
    fun normalizePath_unify_separators_and_trim_tail() {
        Assert.assertEquals("a/b/c", "a//b\\c/".normalizePath())
        Assert.assertEquals("", "".normalizePath())
        Assert.assertEquals("a", "a////".normalizePath())
    }

    @Test
    fun regex_and_ant_wrappers() {
        Assert.assertTrue("foo/bar".matchesAntPattern("foo/**"))
        Assert.assertTrue("abc".matchesRegex("[a-z]+"))
        Assert.assertFalse("abc".matchesRegex("[0-9]+"))
    }
}
