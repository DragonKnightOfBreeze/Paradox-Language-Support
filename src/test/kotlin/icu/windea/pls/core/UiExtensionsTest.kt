package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import javax.swing.Icon
import javax.swing.JLabel

class UiExtensionsTest {
    // region 纯逻辑 / 轻量 Swing 部分

    @Test
    fun colorComponents_test() {
        val (r, g, b, a) = Color(1, 2, 3, 4)
        Assert.assertEquals(1, r)
        Assert.assertEquals(2, g)
        Assert.assertEquals(3, b)
        Assert.assertEquals(4, a)
    }

    private class TestIcon(private val name: String) : Icon {
        override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {}
        override fun getIconWidth() = 16
        override fun getIconHeight() = 16
        override fun equals(other: Any?) = other is TestIcon && name == other.name
        override fun hashCode() = name.hashCode()
    }

    @Test
    fun delegatedIcon_test() {
        val d1 = DelegatedIcon(TestIcon("a"))
        val d2 = DelegatedIcon(TestIcon("a"))
        // 委托原始图标
        Assert.assertEquals(16, d1.iconWidth)
        Assert.assertEquals(16, d1.iconHeight)
        // equals 基于 delegate 的内容判等
        Assert.assertEquals(d1, d2)
        Assert.assertEquals(d1.hashCode(), d2.hashCode())
        // 与原始图标不相等（类型不同）
        Assert.assertFalse(d1.equals(TestIcon("a")))
    }

    @Test
    fun withLocation_test() {
        val label = JLabel()
        Assert.assertSame(label, label.withLocation(3, 4))
        Assert.assertEquals(3, label.location.x)
        Assert.assertEquals(4, label.location.y)
    }

    // endregion

    // region 可观察属性部分

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

    // endregion
}
