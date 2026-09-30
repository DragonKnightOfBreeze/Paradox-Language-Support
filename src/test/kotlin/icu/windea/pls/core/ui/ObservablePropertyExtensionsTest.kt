package icu.windea.pls.core.ui

import org.junit.Assert
import org.junit.Test

class ObservablePropertyExtensionsTest {
    @Test
    fun toAtomicProperty_boolean_test() {
        class H(var flag: Boolean = false)

        val h = H()
        val p = h::flag.toAtomicProperty()
        Assert.assertFalse(p.get())
        p.set(true)
        Assert.assertTrue(h.flag)
        Assert.assertTrue(p.get())
    }

    @Test
    fun toAtomicProperty_generic_test() {
        class H(var name: String = "a")

        val h = H()
        val p = h::name.toAtomicProperty()
        Assert.assertEquals("a", p.get())
        p.set("b")
        Assert.assertEquals("b", h.name)
        Assert.assertEquals("b", p.get())
    }

    @Test
    fun toAtomicProperty_nullableWithDefault_test() {
        class H(var name: String? = null)

        val h = H()
        val p = h::name.toAtomicProperty("default")
        Assert.assertEquals("default", p.get())
        Assert.assertNull(h.name) // 默认值不回写
        p.set("b")
        Assert.assertEquals("b", h.name)
        Assert.assertEquals("b", p.get())
    }
}
