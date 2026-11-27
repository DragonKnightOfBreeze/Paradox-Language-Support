package icu.windea.pls.test.issues

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.ep.resolve.config.CwtRelatedConfigProvider
import icu.windea.pls.test.ChronicleTestScope
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#459](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/459)
 *
 * Various EP interfaces eagerly computed their `LazyValue` caches inside the companion object
 * `init { ... }` block. That block runs during `<clinit>`, so reading `EP_NAME.extensionList` while
 * a read action was unavailable or the thread was canceled (`ReadAction.CannotReadException` /
 * `ProcessCanceledException`) made the class initialization fail permanently with
 * `ExceptionInInitializerError`, because `<clinit>` is only attempted once per class.
 *
 * Cause: `CACHE.initialize { ... }` / `CACHE.reinitialize { ... }` was invoked eagerly from `init`,
 * instead of lazily from the cache read entry point.
 *
 * Solution: `init { ... }` only registers `EP_NAME.addExtensionPointListener { ... }`; the first cache
 * read calls `CACHE.initialize { ... }`. A throwing (e.g. canceled) initializer never marks the value
 * as initialized, so a later retry can complete normally and the class load never executes EP reads.
 *
 * see:
 * - `src/main/kotlin/icu/windea/pls/ep/resolve/config/CwtRelatedConfigProvider.kt`
 * - `src/main/kotlin/icu/windea/pls/core/util/values/LazyValue.kt`
 *
 * @see LazyValue
 * @see CwtRelatedConfigProvider
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue459Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    // region LazyValue

    @Test
    fun testLazyValue_doesNotMarkInitializedWhenInitializerCanceled() {
        val lazyValue = LazyValue<String>()

        Assert.assertThrows(ProcessCanceledException::class.java) {
            lazyValue.initialize { throw ProcessCanceledException() }
        }

        // A failed (e.g. canceled) initialization must not poison the value.
        Assert.assertFalse(lazyValue.isInitialized())
        Assert.assertNull(lazyValue.getOrNull())

        // Retrying after cancellation must run the initializer again and succeed.
        Assert.assertEquals("recovered", lazyValue.initialize { "recovered" })
        Assert.assertTrue(lazyValue.isInitialized())
        Assert.assertEquals("recovered", lazyValue.get())
        Assert.assertEquals("recovered", lazyValue.getOrNull())
    }

    // endregion

    // region EP cache

    @Test
    fun testEpCache_isInitializedLazily() {
        // Accessing `CACHE` triggers the companion object's `<clinit>`.
        // The cache must still be uninitialized here; otherwise it was eagerly computed from `init { ... }`.
        val cache = CwtRelatedConfigProvider.CACHE
        Assert.assertFalse("EP cache must not be computed during <clinit>", cache.isInitialized())

        // The first query lazily initializes the cache.
        val providers = CwtRelatedConfigProvider.getAll()
        Assert.assertTrue(cache.isInitialized())
        Assert.assertFalse("Expected registered related config providers", providers.isEmpty())
    }

    // endregion
}
