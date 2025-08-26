package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test

class DebugExtensionsPureTest {
    @Test
    fun withMeasureMillis_updates_avg_when_debug_true() {
        // ensure debug enabled before first access to DebugExtensions.kt top-level vals
        System.setProperty("pls.is.debug", "true")
        avgMillisMap.clear()

        val id = "test.measure"
        val result = withMeasureMillis(id) {
            Thread.sleep(2)
            123
        }
        Assert.assertEquals(123, result)
        // avg map should contain entry when debug is true
        val avg = avgMillisMap[id]
        Assert.assertTrue(avg != null && avg >= 0.0)

        // call again to update average
        withMeasureMillis(id) { Thread.sleep(2) }
        val avg2 = avgMillisMap[id]
        Assert.assertTrue(avg2 != null && avg2 >= 0.0)
    }

    @Test
    fun withMeasureMillis_returns_value_regardless_mode() {
        val value = withMeasureMillis("noop", min = -1) { 7 }
        Assert.assertEquals(7, value)
    }
}
