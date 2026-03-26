package icu.windea.pls.lang.match

import icu.windea.pls.core.runCatchingCancelable

/**
 * 匹配结果。
 *
 * @see ParadoxMatchService
 */
sealed interface ParadoxMatchResult {
    fun get(options: ParadoxMatchOptions? = null): Boolean

    /** 直接匹配。 */
    sealed interface DirectMatch : ParadoxMatchResult

    /** 延迟匹配。需要按照具体的类型，每次一组依次尝试应用。 */
    sealed interface DeferredMatch : ParadoxMatchResult

    /** 绝对不匹配。 */
    data object NotMatch : ParadoxMatchResult, DirectMatch {
        override fun get(options: ParadoxMatchOptions?) = false
    }

    /** 精确匹配。 */
    data object ExactMatch : ParadoxMatchResult, DirectMatch {
        override fun get(options: ParadoxMatchOptions?) = true
    }

    /** 可容忍的精确匹配。在语义解析阶段会认为不是正确的表达式，因而给出警告或错误。 */
    data object ToleratedExactMatch : ParadoxMatchResult, DirectMatch {
        override fun get(options: ParadoxMatchOptions?) = true
    }

    /** 通配符匹配。不验证其中某部分在解析引用后是否合法。 */
    data object WildcardMatch : ParadoxMatchResult, DeferredMatch {
        override fun get(options: ParadoxMatchOptions?) = true
    }

    /** 更宽松的通配符匹配。这意味着存在另一种更精确的格式。 */
    data object RelaxWildcardMatch : ParadoxMatchResult, DeferredMatch {
        override fun get(options: ParadoxMatchOptions?) = true
    }

    /** 部分匹配。这意味着其中某部分存在格式上的错误。 */
    data object PartialMatch : ParadoxMatchResult, DeferredMatch {
        override fun get(options: ParadoxMatchOptions?) = true
    }

    /** 回退匹配。其他所有常规匹配都无法应用时才会考虑的匹配。 */
    data object FallbackMatch : ParadoxMatchResult, DeferredMatch {
        override fun get(options: ParadoxMatchOptions?) = true
    }

    /** 带参数的匹配。 */
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
                this is LazyBlockAwareMatch -> ParadoxMatchService.relax(options)
                this is LazyIndexAwareMatch -> ParadoxMatchService.skipIndex(options)
                this is LazyScopeAwareMatch -> ParadoxMatchService.skipScope(options)
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
