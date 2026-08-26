package icu.windea.pls.core.util.values

import org.junit.Assert
import org.junit.Test

/**
 * @see FallbackValue
 */
class FallbackValueTest {
    @Test
    fun fallback_strings_constants_test() {
        Assert.assertEquals("(anonymous)", FallbackStrings.anonymous)
        Assert.assertEquals("(unknown)", FallbackStrings.unknown)
        Assert.assertEquals("(unresolved)", FallbackStrings.unresolved)
        Assert.assertEquals("(unnamed)", FallbackStrings.unnamed)
        Assert.assertEquals("(injected)", FallbackStrings.injected)
    }

    @Test
    fun or_extension_test() {
        Assert.assertEquals("a", "a".or.value)
        Assert.assertNull(null.or.value)
    }

    @Test
    fun anonymous_unknown_unresolved_test() {
        Assert.assertEquals("a", "a".or.anonymous())
        Assert.assertEquals("(anonymous)", "".or.anonymous())
        Assert.assertEquals("(anonymous)", null.or.anonymous())
        Assert.assertEquals("(unknown)", null.or.unknown())
        Assert.assertEquals("(unresolved)", "".or.unresolved())
    }
}
