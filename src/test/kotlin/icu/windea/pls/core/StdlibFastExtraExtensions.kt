package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test

class StdlibFastExtraExtensions {
    @Test
    fun matchesPath_basic_and_strict_test() {
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
    fun matchesPath_trim_test() {
        // when trim=true, only the receiver is trimmed
        Assert.assertTrue("a/b/".matchesPath("a/b/c", trim = true))
        Assert.assertTrue("a/b".matchesPath("a/b/c", trim = false))
        Assert.assertFalse("a/b/".matchesPath("a-b/c", trim = true))
    }
}
