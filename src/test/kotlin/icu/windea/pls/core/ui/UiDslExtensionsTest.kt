package icu.windea.pls.core.ui

import org.junit.Assert
import org.junit.Test

class UiDslExtensionsTest {
    @Test
    fun toMutableProperty_test() {
        val map = mutableMapOf<String, Int>()
        val p = map.toMutableProperty("a", 42)
        Assert.assertEquals(42, p.get())
        Assert.assertEquals(42, map["a"]) // getOrPut 会回填默认值
        p.set(5)
        Assert.assertEquals(5, map["a"])
        Assert.assertEquals(5, p.get())
    }
}
