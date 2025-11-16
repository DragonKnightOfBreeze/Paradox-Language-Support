package icu.windea.pls.lang.match

import com.intellij.util.BitUtil
import icu.windea.pls.core.runCatchingCancelable

/**
 * 匹配结果。
 *
 * @see ParadoxMatchService
 */
sealed class ParadoxMatchResult {
    abstract fun get(options: Int = ParadoxMatchOptions.Default): Boolean

    data object NotMatch : ParadoxMatchResult() {
        override fun get(options: Int) = false
    }

    data object ExactMatch : ParadoxMatchResult() {
        override fun get(options: Int) = true
    }

    data object FallbackMatch : ParadoxMatchResult() {
        override fun get(options: Int) = true
    }

    data object PartialMatch : ParadoxMatchResult() {
        override fun get(options: Int) = true
    }

    data object ParameterizedMatch : ParadoxMatchResult() {
        override fun get(options: Int) = true
    }

    sealed class LazyMatch(predicate: () -> Boolean) : ParadoxMatchResult() {
        // use manual lazy implementation instead of kotlin Lazy to optimize memory
        @Volatile
        private var value: Any = predicate

        override fun get(options: Int): Boolean {
            if (skip(options)) return true
            if (value is Boolean) return value as Boolean
            val r = doGetCatching()
            value = r
            return r
        }

        private fun skip(options: Int): Boolean {
            return when {
                this is LazySimpleMatch -> BitUtil.isSet(options, ParadoxMatchOptions.Relax)
                this is LazyBlockAwareMatch -> BitUtil.isSet(options, ParadoxMatchOptions.Relax)
                this is LazyIndexAwareMatch -> ParadoxMatchUtil.skipIndex(options)
                this is LazyScopeAwareMatch -> ParadoxMatchUtil.skipScope(options)
                else -> false
            }
        }

        private fun doGetCatching(): Boolean {
            // it should be necessary to suppress outputting error logs and throwing exceptions here

            // java.lang.Throwable: Indexing process should not rely on non-indexed file data.
            // java.lang.AssertionError: Reentrant indexing
            // com.intellij.openapi.project.IndexNotReadyException

            return runCatchingCancelable {
                @Suppress("UNCHECKED_CAST")
                (value as () -> Boolean)()
            }.getOrDefault(true)
        }
    }

    class LazySimpleMatch(predicate: () -> Boolean) : LazyMatch(predicate)

    class LazyBlockAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate)

    class LazyIndexAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate)

    class LazyScopeAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate)

    companion object {
        fun of(value: Boolean) = if (value) ExactMatch else NotMatch

        fun ofFallback(value: Boolean) = if (value) FallbackMatch else NotMatch
    }
}
