package icu.windea.pls.core.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.LoadingCache
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import org.junit.Assert
import org.junit.Test

class CachesTest {
    // region CacheBuilder

    @Test
    fun cacheBuilder_buildAndHit_test() {
        var count = 0
        val cache: Cache<String, Int> = CacheBuilder().build()
        Assert.assertEquals(3, cache.get("abc") { count++; 3 })
        Assert.assertEquals(3, cache.get("abc") { count++; 999 }) // 命中缓存，不重新计算
        Assert.assertEquals(1, count)
    }

    @Test
    fun cacheBuilder_spec_test() {
        // 带 spec 构建缓存（这里仅验证能正常构建与使用）
        val cache: Cache<String, Int> = CacheBuilder("maximumSize=100").build()
        Assert.assertEquals(1, cache.get("a") { 1 })
    }

    // endregion

    // region CancelableCache / CancelableLoadingCache

    @Test
    fun cancelableCache_pceRethrown_test() {
        val cache = CacheBuilder().build<String, Int>().cancelable()
        Assert.assertEquals(1, cache.get("a") { 1 })
        Assert.assertThrows(ProcessCanceledException::class.java) {
            cache.get("b") { throw ProcessCanceledException() }
        }
    }

    @Test
    fun cancelableLoadingCache_pceRethrown_test() {
        val cache = CacheBuilder().build<String, Int> { k -> k.length }.cancelable()
        Assert.assertEquals(3, cache.get("abc"))
        Assert.assertThrows(ProcessCanceledException::class.java) {
            cache.get("bad") { throw ProcessCanceledException() }
        }
    }

    // endregion

    // region TrackingCache

    @Test
    fun trackingCache_hitAndInvalidateOnChange_test() {
        val tracker = SimpleModificationTracker()
        var count = 0
        val cache = CacheBuilder().build<String, String>().trackedBy { tracker }

        Assert.assertEquals("v1", cache.get("key") { count++; "v1" })
        Assert.assertEquals(1, count)
        // 计数未变化：命中缓存，不重新计算
        Assert.assertEquals("v1", cache.get("key") { count++; "v2" })
        Assert.assertEquals(1, count)
        // 模拟发生更改：直接自增修改计数
        tracker.incModificationCount()
        Assert.assertEquals("v2", cache.get("key") { count++; "v2" })
        Assert.assertEquals(2, count)
    }

    @Test
    fun trackingCache_getIfPresent_test() {
        val tracker = SimpleModificationTracker()
        val cache = CacheBuilder().build<String, String>().trackedBy { tracker }

        Assert.assertNull(cache.getIfPresent("key"))
        cache.get("key") { "v1" }
        Assert.assertEquals("v1", cache.getIfPresent("key"))
        // 模拟发生更改后，getIfPresent 应失效并返回 null
        tracker.incModificationCount()
        Assert.assertNull(cache.getIfPresent("key"))
    }

    @Test
    fun trackingCache_invalidate_test() {
        val tracker = SimpleModificationTracker()
        var count = 0
        val cache = CacheBuilder().build<String, String>().trackedBy { tracker }

        cache.get("key") { count++; "v1" }
        cache.invalidate("key")
        Assert.assertEquals("v2", cache.get("key") { count++; "v2" })
        Assert.assertEquals(2, count)
    }

    @Test
    fun trackingCache_neverChangedTracker_test() {
        var count = 0
        // NEVER_CHANGED 追踪器：值永远不会被失效
        val cache = CacheBuilder().build<String, String>().trackedBy { ModificationTracker.NEVER_CHANGED }
        Assert.assertEquals("v1", cache.get("key") { count++; "v1" })
        Assert.assertEquals("v1", cache.get("key") { count++; "v2" })
        Assert.assertEquals(1, count)
    }

    // endregion

    // region TrackingLoadingCache

    @Test
    fun trackingLoadingCache_hitAndInvalidateOnChange_test() {
        val tracker = SimpleModificationTracker()
        var count = 0
        val cache: LoadingCache<String, String> = CacheBuilder().build<String, String> { count++; "loaded:$it" }.trackedBy { tracker }

        Assert.assertEquals("loaded:key", cache.get("key"))
        Assert.assertEquals(1, count)
        Assert.assertEquals("loaded:key", cache.get("key")) // 命中
        Assert.assertEquals(1, count)
        tracker.incModificationCount()
        Assert.assertEquals("loaded:key", cache.get("key")) // 失效后重新载入
        Assert.assertEquals(2, count)
    }

    // endregion

    // region NestedCache / NestedLoadingCache

    @Test
    fun nestedCache_lazyPerKey_test() {
        var created = 0
        val nested = createNestedCache<String, String, Int> { created++; CacheBuilder().build() }
        val inner1 = nested["a"]
        val inner2 = nested["a"]
        Assert.assertSame(inner1, inner2) // 同一外层键复用同一内部缓存
        val inner3 = nested["b"]
        Assert.assertNotSame(inner1, inner3)
        Assert.assertEquals(2, created) // 每个外层键仅创建一次
    }

    @Test
    fun nestedLoadingCache_test() {
        val nested = createNestedLoadingCache<String, String, Int> { CacheBuilder().build { k -> k.length } }
        val inner = nested["a"]
        Assert.assertEquals(3, inner.get("abc"))
        Assert.assertSame(inner, nested["a"])
    }

    // endregion
}
