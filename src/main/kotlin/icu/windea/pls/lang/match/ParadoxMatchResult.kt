package icu.windea.pls.lang.match

import icu.windea.pls.core.runCatchingCancelable

/**
 * 匹配结果。
 *
 * @see ParadoxMatchService
 */
sealed interface ParadoxMatchResult {
    fun get(options: ParadoxMatchOptions? = null): Boolean

    sealed interface DirectMatch : ParadoxMatchResult

    sealed interface DeferredMatch : ParadoxMatchResult

    data object NotMatch : ParadoxMatchResult, DirectMatch {
        override fun get(options: ParadoxMatchOptions?) = false
    }

    data object ExactMatch : ParadoxMatchResult, DirectMatch {
        override fun get(options: ParadoxMatchOptions?) = true
    }

    data object WildcardMatch : ParadoxMatchResult, DeferredMatch {
        override fun get(options: ParadoxMatchOptions?) = true
    }

    data object PartialMatch : ParadoxMatchResult, DeferredMatch {
        override fun get(options: ParadoxMatchOptions?) = true
    }

    data object FallbackMatch : ParadoxMatchResult, DeferredMatch {
        override fun get(options: ParadoxMatchOptions?) = true
    }

    data object ParameterizedMatch : ParadoxMatchResult {
        override fun get(options: ParadoxMatchOptions?) = true
    }

    sealed class LazyMatch(predicate: () -> Boolean) : ParadoxMatchResult {
        // use manual lazy implementation instead of kotlin Lazy to optimize memory
        @Volatile
        private var value: Any = predicate

        override fun get(options: ParadoxMatchOptions?): Boolean {
            if (skip(options)) return true
            if (value is Boolean) return value as Boolean
            val r = doGetCatching()
            value = r
            return r
        }

        private fun skip(options: ParadoxMatchOptions?): Boolean {
            return when {
                this is LazyBlockAwareMatch -> ParadoxMatchOptionsUtil.relax(options)
                this is LazyIndexAwareMatch -> ParadoxMatchOptionsUtil.skipIndex(options)
                this is LazyScopeAwareMatch -> ParadoxMatchOptionsUtil.skipScope(options)
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

    class LazyTemplateAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate), DirectMatch

    class LazyIndexAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate), DirectMatch

    class LazyBlockAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate), DirectMatch

    class LazyScopeAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate), DirectMatch

    companion object {
        fun exactOrNot(value: Boolean) = if (value) ExactMatch else NotMatch

        fun fallbackOrNot(value: Boolean) = if (value) FallbackMatch else NotMatch

        fun exactOrFallback(value: Boolean) = if (value) ExactMatch else FallbackMatch
    }
}
