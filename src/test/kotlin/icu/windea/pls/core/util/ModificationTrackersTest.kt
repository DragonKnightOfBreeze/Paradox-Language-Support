package icu.windea.pls.core.util

import com.intellij.openapi.util.SimpleModificationTracker
import org.junit.Assert
import org.junit.Test

/**
 * @see ModificationTrackers
 */
class ModificationTrackersTest {
    @Test
    fun mergedModificationTracker_test() {
        val t1 = SimpleModificationTracker()
        val t2 = SimpleModificationTracker()
        val merged = MergedModificationTracker(t1, t2)
        Assert.assertEquals(0L, merged.modificationCount)
        t1.incModificationCount()
        Assert.assertEquals(1L, merged.modificationCount)
        t2.incModificationCount()
        t2.incModificationCount()
        Assert.assertEquals(3L, merged.modificationCount)
    }

    @Test
    fun computedModificationTracker_changeIncrements_test() {
        var value = 1
        val tracker = ComputedModificationTracker { value }
        Assert.assertEquals(0L, tracker.modificationCount) // 首次不计
        Assert.assertEquals(0L, tracker.modificationCount) // 未变化
        value = 2
        Assert.assertEquals(1L, tracker.modificationCount) // 变化
        Assert.assertEquals(1L, tracker.modificationCount) // 未变化
        value = 3
        Assert.assertEquals(2L, tracker.modificationCount) // 再次变化
    }

    @Test
    fun computedModificationTracker_nullValue_test() {
        // 计算值始终为 null 时应视为无变化（回归：此前 null->null 会误自增）
        val tracker = ComputedModificationTracker { null }
        Assert.assertEquals(0L, tracker.modificationCount)
        Assert.assertEquals(0L, tracker.modificationCount)
        Assert.assertEquals(0L, tracker.modificationCount)
    }
}
