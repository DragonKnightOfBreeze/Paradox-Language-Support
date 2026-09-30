package icu.windea.pls.core

import icu.windea.pls.core.ui.component1
import icu.windea.pls.core.ui.component2
import icu.windea.pls.core.ui.component3
import icu.windea.pls.core.ui.component4
import icu.windea.pls.core.ui.withLocation
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import javax.swing.JLabel

class UiExtensionsTest {
    @Test
    fun colorComponents_test() {
        val (r, g, b, a) = Color(1, 2, 3, 4)
        Assert.assertEquals(1, r)
        Assert.assertEquals(2, g)
        Assert.assertEquals(3, b)
        Assert.assertEquals(4, a)
    }

    @Test
    fun withLocation_test() {
        val label = JLabel()
        Assert.assertSame(label, label.withLocation(3, 4))
        Assert.assertEquals(3, label.location.x)
        Assert.assertEquals(4, label.location.y)
    }
}
