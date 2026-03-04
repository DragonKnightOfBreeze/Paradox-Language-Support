package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.collections.filterFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.ep.match.ParadoxScriptExpressionMatchOptimizer
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.dynamic
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression

object ParadoxMatchPipeline {
    /**
     * 根据来自 [matchResultProvider] 的匹配结果，从输入的一组成员规则 [configs] 收集匹配候选项。
     */
    @Optimized
    inline fun <T : CwtMemberConfig<*>> collectCandidates(configs: List<T>, matchResultProvider: (T) -> ParadoxMatchResult): List<ParadoxMatchCandidate> {
        if (configs.isEmpty()) return emptyList()
        val result = buildList {
            configs.forEachFast f@{ config ->
                val matchResult = matchResultProvider(config)
                if (matchResult == ParadoxMatchResult.NotMatch) return@f
                val matchCandidate = ParadoxMatchCandidate(config, matchResult)
                this += matchCandidate
            }
        }
        return result
    }

    /**
     * 处理输入的一组匹配候选项 [candidates]，进行进一步的匹配。基于匹配结果的类型。
     */
    @Optimized
    fun process(candidates: List<ParadoxMatchCandidate>, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        // 首先尝试直接的精确匹配，如果有结果，则直接返回
        // 然后，尝试需要检测子句的匹配，如果存在匹配项，则保留所有匹配的结果或者第一个匹配项
        // 然后，尝试需要检测作用域上下文的匹配，如果存在匹配项，则保留所有匹配的结果或者第一个匹配项
        // 然后，尝试非部分非回退的匹配，如果有结果，则直接返回
        // 然后，尝试部分匹配（可以部分解析为复杂表达式，但存在错误），如果有结果，则直接返回
        // 然后，尝试回退匹配，如果有结果，则直接返回
        // 如果到这里仍然无法匹配，则直接返回空列表

        if (candidates.isEmpty()) return emptyList()

        val matched = FastList<ParadoxMatchCandidate>()

        processMatched(candidates, matched) { it.result is ParadoxMatchResult.ExactMatch }
        if (matched.isNotEmpty()) return matched.mapFast { it.value }

        processLazyMatched(candidates, matched, options) { it.result is ParadoxMatchResult.LazyBlockAwareMatch }
        processLazyMatched(candidates, matched, options) { it.result is ParadoxMatchResult.LazyScopeAwareMatch }

        processDirectMatched(candidates, matched, options)
        if (matched.isNotEmpty()) return matched.mapFast { it.value }

        processMatched(candidates, matched) { it.result is ParadoxMatchResult.PartialMatch }
        if (matched.isNotEmpty()) return matched.mapFast { it.value }
        processMatched(candidates, matched) { it.result is ParadoxMatchResult.FallbackMatch }
        if (matched.isNotEmpty()) return matched.mapFast { it.value }

        return emptyList()
    }

    private inline fun processMatched(candidates: List<ParadoxMatchCandidate>, matched: FastList<ParadoxMatchCandidate>, predicate: (ParadoxMatchCandidate) -> Boolean) {
        candidates.forEachFast f@{
            if (predicate(it)) matched += it
        }
    }

    private inline fun processLazyMatched(candidates: List<ParadoxMatchCandidate>, matched: MutableList<ParadoxMatchCandidate>, options: ParadoxMatchOptions?, predicate: (ParadoxMatchCandidate) -> Boolean) {
        val lazyMatched = candidates.filterFast(predicate)
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

    private fun processDirectMatched(candidates: List<ParadoxMatchCandidate>, matched: FastList<ParadoxMatchCandidate>, options: ParadoxMatchOptions?) {
        candidates.forEachFast f@{
            if (it.result is ParadoxMatchResult.LazyRangedMatch) return@f run { matched += it } // 直接认为是匹配的
            if (it.result is ParadoxMatchResult.LazyBlockAwareMatch) return@f // 已经匹配过
            if (it.result is ParadoxMatchResult.LazyScopeAwareMatch) return@f // 已经匹配过
            if (it.result is ParadoxMatchResult.PartialMatch) return@f // 之后再匹配
            if (it.result is ParadoxMatchResult.FallbackMatch) return@f // 之后再匹配
            if (!it.result.get(options)) return@f
            matched += it
        }
    }

    /**
     * 对输入的一组已处理过的成员规则 [configs] 进行后续优化。基于 [ParadoxScriptExpressionMatchOptimizer]。
     */
    @Optimized
    fun <T : CwtMemberConfig<*>> optimize(configs: List<T>, element: PsiElement, expression: ParadoxScriptExpression, options: ParadoxMatchOptions? = null): List<T> {
        if (configs.isEmpty()) return emptyList()
        val configGroup = configs.first().configGroup
        var result = configs
        var dynamic = false

        val context = ParadoxScriptExpressionMatchOptimizerContext(element, expression, configGroup, options)
        val optimizers = ParadoxScriptExpressionMatchOptimizer.EP_NAME.extensionList
        optimizers.forEachFast f@{ optimizer ->
            val optimized = optimizer.optimize(result, context)
            if (optimized == null) return@f
            if (optimizer.isDynamic(context)) dynamic = true
            result = optimized
        }

        // NOTE 2.1.2 如果是动态的优化器，需要把正在解析的规则上下文标记为动态的
        if (dynamic) PlsStates.resolvingConfigContextStack.get()?.peekLast()?.dynamic = true

        return result
    }
}
