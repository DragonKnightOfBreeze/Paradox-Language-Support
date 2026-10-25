package icu.windea.pls.core.util.markers

import org.junit.Assert
import org.junit.Test

/**
 * @see ToggleMarker
 */
class ToggleMarkerTest {
    @Test
    fun smokeTest() {
        val m = ToggleMarker()
        Assert.assertFalse(m.get())
        Assert.assertFalse(m.mark()) // 切换前为 false
        Assert.assertTrue(m.get())
        Assert.assertTrue(m.mark()) // 切换前为 true
        Assert.assertFalse(m.get())
        m.reset()
        Assert.assertFalse(m.get())
    }
}

