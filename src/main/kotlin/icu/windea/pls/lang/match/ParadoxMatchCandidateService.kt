package icu.windea.pls.lang.match

import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.collections.forEachFast

object ParadoxMatchCandidateService {
    @Optimized
    fun process(candidates: List<ParadoxMatchCandidate>, options: ParadoxMatchOptions?): List<ParadoxMatchCandidate> {
        // 步骤：
        // - 处理精确匹配（`ExactMatch` `ToleratedExactMatch`），如果有结果，则仅使用这些结果，并直接返回
        // - 处理需要检测子句内容的匹配（`LazyBlockAwareMatch`），如果存在匹配项，则保留所有匹配项或者第一个候选项
        // - 处理需要检测作用域上下文的匹配（`LazyScopeAwareMatch`），如果存在匹配项，则保留所有匹配项或者第一个候选项
        // - 处理其余的各种直接匹配，如果有结果，则仅使用这些结果
        // - 处理通配符匹配（`WildcardMatch`，不验证其中某部分在解析引用后是否合法），如果有结果，则仅使用这些结果
        // - 处理更宽松的通配符匹配（`RelaxWildcardMatch`，这意味着存在另一种更精确的格式），如果有结果，则仅使用这些结果
        // - 处理部分匹配（`PartialMatch`），如果有结果，则仅使用这些结果
        // - 处理回退匹配（`FallbackMatch`），如果有结果，则仅使用这些结果
        // - 如果不是直接返回的情况，还需要处理带参数的匹配（`ParameterizedMatch`），如果有结果，则需要加入最终的结果中

        if (candidates.isEmpty()) return emptyList()
        val matched = FastList<ParadoxMatchCandidate>()
        process(candidates, matched, options)
        return matched
    }

    private fun process(candidates: List<ParadoxMatchCandidate>, matched: MutableList<ParadoxMatchCandidate>, options: ParadoxMatchOptions?) {
        processUnchecked(candidates, matched) { it.result is ParadoxMatchResult.ExactMatch || it.result is ParadoxMatchResult.ToleratedExactMatch }
        if (matched.isNotEmpty()) return

        processMain(candidates, matched, options)
        processUnchecked(candidates, matched) { it.result is ParadoxMatchResult.ParameterizedMatch }
    }

    private fun processMain(candidates: List<ParadoxMatchCandidate>, matched: MutableList<ParadoxMatchCandidate>, options: ParadoxMatchOptions?) {
        processLazy(candidates, matched, options) { it.result is ParadoxMatchResult.LazyBlockAwareMatch }
        processLazy(candidates, matched, options) { it.result is ParadoxMatchResult.LazyScopeAwareMatch }

        processChecked(candidates, matched, options) { it.result is ParadoxMatchResult.DirectMatch }
        if (matched.isNotEmpty()) return

        processChecked(candidates, matched, options) { it.result is ParadoxMatchResult.WildcardMatch }
        if (matched.isNotEmpty()) return
        processChecked(candidates, matched, options) { it.result is ParadoxMatchResult.RelaxWildcardMatch }
        if (matched.isNotEmpty()) return
        processChecked(candidates, matched, options) { it.result is ParadoxMatchResult.PartialMatch }
        if (matched.isNotEmpty()) return

        processChecked(candidates, matched, options) { it.result is ParadoxMatchResult.FallbackMatch }
    }

    private inline fun processChecked(candidates: List<ParadoxMatchCandidate>, matched: MutableList<ParadoxMatchCandidate>, options: ParadoxMatchOptions?, predicate: (ParadoxMatchCandidate) -> Boolean) {
        candidates.forEachFast f@{
            if (it.processed) return@f
            if (!predicate(it)) return@f
            it.processed = true
            if (!it.result.get(options)) return@f
            matched += it
        }
    }

    private inline fun processUnchecked(candidates: List<ParadoxMatchCandidate>, matched: MutableList<ParadoxMatchCandidate>, predicate: (ParadoxMatchCandidate) -> Boolean) {
        candidates.forEachFast f@{
            if (it.processed) return@f
            if (!predicate(it)) return@f
            it.processed = true
            matched += it
        }
    }

    private inline fun processLazy(candidates: List<ParadoxMatchCandidate>, matched: MutableList<ParadoxMatchCandidate>, options: ParadoxMatchOptions?, predicate: (ParadoxMatchCandidate) -> Boolean) {
        val lazyMatched = FastList<ParadoxMatchCandidate>()
        processUnchecked(candidates, lazyMatched, predicate)
        val lazyMatchedSize = lazyMatched.size
        if (lazyMatchedSize == 1) {
            matched += lazyMatched.first()
        } else if (lazyMatchedSize > 1) {
            val oldMatchedSize = matched.size
            lazyMatched.forEachFast f@{
                if (!it.result.get(options)) return@f
                matched += it
            }
            if (oldMatchedSize == matched.size) matched += lazyMatched.first()
        }
    }
}
