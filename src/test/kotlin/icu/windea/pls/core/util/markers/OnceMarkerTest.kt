package icu.windea.pls.core.util.markers

import org.junit.Assert
import org.junit.Test

/**
 * @see OnceMarker
 */
class OnceMarkerTest {
    @Test
    fun smokeTest() {
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
