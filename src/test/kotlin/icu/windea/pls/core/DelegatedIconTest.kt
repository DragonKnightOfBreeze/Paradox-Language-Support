package icu.windea.pls.core

import icu.windea.pls.core.ui.DelegatedIcon
import org.junit.Assert
import org.junit.Test
import java.awt.Component
import java.awt.Graphics
import javax.swing.Icon

class DelegatedIconTest {
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
}
