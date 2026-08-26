package icu.windea.pls.core.util

import org.junit.Assert
import org.junit.Test

/**
 * @see ToggleMarker
 * @see OnceMarker
 */
class MarkersTest {
    @Test
    fun toggleMarker_test() {
        val m = ToggleMarker()
        Assert.assertFalse(m.get())
        Assert.assertFalse(m.mark()) // 切换前为 false
        Assert.assertTrue(m.get())
        Assert.assertTrue(m.mark()) // 切换前为 true
        Assert.assertFalse(m.get())
        m.reset()
        Assert.assertFalse(m.get())
    }

    @Test
    fun onceMarker_test() {
        val m = OnceMarker()
        Assert.assertFalse(m.get())
        Assert.assertFalse(m.mark()) // 首次标记
        Assert.assertTrue(m.get())
        Assert.assertTrue(m.mark()) // 已标记，返回 true
        Assert.assertTrue(m.get())
        m.reset()
        Assert.assertFalse(m.get())
    }
}
