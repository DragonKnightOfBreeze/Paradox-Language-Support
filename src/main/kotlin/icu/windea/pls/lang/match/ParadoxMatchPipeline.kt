package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.ep.match.ParadoxScriptExpressionMatchOptimizer
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression

object ParadoxMatchPipeline {
    inline fun <T : CwtMemberConfig<*>> collectCandidates(
        configs: List<T>,
        matchResultProvider: (T) -> ParadoxMatchResult?
    ): List<ParadoxMatchCandidate> {
        if (configs.isEmpty()) return emptyList()
        val result = buildList {
            for (config in configs) {
                val matchResult = matchResultProvider(config)
                if (matchResult == null || matchResult == ParadoxMatchResult.NotMatch) continue
                val matchCandidate = ParadoxMatchCandidate(config, matchResult)
                this += matchCandidate
            }
        }
        return result
    }

    fun filter(
        candidates: List<ParadoxMatchCandidate>,
        matchOptions: Int = ParadoxMatchOptions.Default
    ): List<CwtMemberConfig<*>> {
        // 首先尝试直接的精确匹配，如果有结果，则直接返回
        // 然后，尝试需要检测子句的匹配，如果存在匹配项，则保留所有匹配的结果或者第一个匹配项
        // 然后，尝试需要检测作用域上下文的匹配，如果存在匹配项，则保留所有匹配的结果或者第一个匹配项
        // 然后，尝试非部分非回退的匹配，如果有结果，则直接返回
        // 然后，尝试部分匹配（可以部分解析为复杂表达式，但存在错误），如果有结果，则直接返回
        // 然后，尝试回退匹配，如果有结果，则直接返回
        // 如果到这里仍然无法匹配，则直接返回空列表

        if (candidates.isEmpty()) return emptyList()
        val exactMatched = candidates.filter { it.result is ParadoxMatchResult.ExactMatch }
        if (exactMatched.isNotEmpty()) return exactMatched.map { it.value }

        val matched = mutableListOf<ParadoxMatchCandidate>()

        addLazyMatchedConfigs(matched, candidates, matchOptions) { it.result is ParadoxMatchResult.LazyBlockAwareMatch }
        addLazyMatchedConfigs(matched, candidates, matchOptions) { it.result is ParadoxMatchResult.LazyScopeAwareMatch }

        candidates.filterTo(matched) p@{
            if (it.result is ParadoxMatchResult.LazyBlockAwareMatch) return@p false // 已经匹配过
            if (it.result is ParadoxMatchResult.LazyScopeAwareMatch) return@p false // 已经匹配过
            if (it.result is ParadoxMatchResult.LazySimpleMatch) return@p true // 直接认为是匹配的
            if (it.result is ParadoxMatchResult.PartialMatch) return@p false // 之后再匹配
            if (it.result is ParadoxMatchResult.FallbackMatch) return@p false // 之后再匹配
            it.result.get(matchOptions)
        }
        if (matched.isNotEmpty()) return matched.map { it.value }

        candidates.filterTo(matched) { it.result is ParadoxMatchResult.PartialMatch }
        if (matched.isNotEmpty()) return matched.map { it.value }

        candidates.filterTo(matched) { it.result is ParadoxMatchResult.FallbackMatch }
        if (matched.isNotEmpty()) return matched.map { it.value }

        return emptyList()
    }

    private fun addLazyMatchedConfigs(
        matched: MutableList<ParadoxMatchCandidate>,
        candidates: List<ParadoxMatchCandidate>,
        matchOptions: Int,
        predicate: (ParadoxMatchCandidate) -> Boolean
    ) {
        val lazyMatched = candidates.filter(predicate)
        val lazyMatchedSize = lazyMatched.size
        if (lazyMatchedSize == 1) {
            matched += lazyMatched.first()
        } else if (lazyMatchedSize > 1) {
            val oldMatchedSize = matched.size
            lazyMatched.filterTo(matched) { it.result.get(matchOptions) }
            if (oldMatchedSize == matched.size) matched += lazyMatched.first()
        }
    }

    fun optimize(
        element: PsiElement,
        expression: ParadoxScriptExpression,
        configs: List<CwtMemberConfig<*>>,
        matchOptions: Int = ParadoxMatchOptions.Default
    ): List<CwtMemberConfig<*>> {
        // 进行后续优化

        if (configs.isEmpty()) return emptyList()
        val configGroup = configs.first().configGroup
        var result = configs
        val context = ParadoxScriptExpressionMatchOptimizer.Context(element, expression, configGroup, matchOptions)
        for (optimizer in ParadoxScriptExpressionMatchOptimizer.EP_NAME.extensionList) {
            val optimized = optimizer.optimize(result, context)
            if (optimized == null) continue
            if (optimizer.isDynamic(context)) markDynamicAfterOptimized()
            result = optimized
            if (result.isEmpty()) break
        }
        return result
    }

    private fun markDynamicAfterOptimized() {
        // see icu.windea.pls.lang.resolve.CwtConfigContext.getConfigs
        val s = PlsStates.dynamicContextConfigs
        if (s.get() == null) return
        s.set(true)
    }
}
