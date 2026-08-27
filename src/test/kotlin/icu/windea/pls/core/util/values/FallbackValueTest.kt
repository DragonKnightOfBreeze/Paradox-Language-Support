package icu.windea.pls.core.util.values

import icu.windea.pls.core.constants.DefaultStrings
import org.junit.Assert
import org.junit.Test

/**
 * @see FallbackValue
 */
class FallbackValueTest {
    @Test
    fun fallback_strings_constants_test() {
        Assert.assertEquals("(anonymous)", DefaultStrings.anonymous)
        Assert.assertEquals("(unknown)", DefaultStrings.unknown)
        Assert.assertEquals("(unresolved)", DefaultStrings.unresolved)
        Assert.assertEquals("(unnamed)", DefaultStrings.unnamed)
        Assert.assertEquals("(injected)", DefaultStrings.injected)
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

    @Test
    fun fallback_onlyNullOrEmpty_test() {
        // 仅 null 或空字符串触发回退，空白字符串（非空）不会触发
        Assert.assertEquals("   ", "   ".or.anonymous())
        Assert.assertEquals("a", "a".or.unknown())
        Assert.assertEquals("a", "a".or.unresolved())
        Assert.assertEquals("(unknown)", "".or.unknown())
        Assert.assertEquals("(unresolved)", null.or.unresolved())
    }
}
