package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.collections.filterFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.ep.match.ParadoxScriptExpressionMatchOptimizer
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.lang.resolve.dynamic
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression

object ParadoxMatchPipeline {
    @Optimized
    inline fun <T : CwtMemberConfig<*>> collectCandidates(
        configs: List<T>,
        matchResultProvider: (T) -> ParadoxMatchResult?
    ): List<ParadoxMatchCandidate> {
        if (configs.isEmpty()) return emptyList()
        val result = buildList {
            configs.forEachFast f@{ config ->
                val matchResult = matchResultProvider(config)
                if (matchResult == null || matchResult == ParadoxMatchResult.NotMatch) return@f
                val matchCandidate = ParadoxMatchCandidate(config, matchResult)
                this += matchCandidate
            }
        }
        return result
    }

    @Optimized
    fun filter(
        candidates: List<ParadoxMatchCandidate>,
        options: ParadoxMatchOptions? = null
    ): List<CwtMemberConfig<*>> {
        // 首先尝试直接的精确匹配，如果有结果，则直接返回
        // 然后，尝试需要检测子句的匹配，如果存在匹配项，则保留所有匹配的结果或者第一个匹配项
        // 然后，尝试需要检测作用域上下文的匹配，如果存在匹配项，则保留所有匹配的结果或者第一个匹配项
        // 然后，尝试非部分非回退的匹配，如果有结果，则直接返回
        // 然后，尝试部分匹配（可以部分解析为复杂表达式，但存在错误），如果有结果，则直接返回
        // 然后，尝试回退匹配，如果有结果，则直接返回
        // 如果到这里仍然无法匹配，则直接返回空列表

        if (candidates.isEmpty()) return emptyList()
        val exactMatched = candidates.filterFast { it.result is ParadoxMatchResult.ExactMatch }
        if (exactMatched.isNotEmpty()) return exactMatched.mapFast { it.value }

        val matched = FastList<ParadoxMatchCandidate>()

        addLazyMatchedConfigs(matched, candidates, options) { it.result is ParadoxMatchResult.LazyBlockAwareMatch }
        addLazyMatchedConfigs(matched, candidates, options) { it.result is ParadoxMatchResult.LazyScopeAwareMatch }

        candidates.filterTo(matched) p@{
            if (it.result is ParadoxMatchResult.LazyBlockAwareMatch) return@p false // 已经匹配过
            if (it.result is ParadoxMatchResult.LazyScopeAwareMatch) return@p false // 已经匹配过
            if (it.result is ParadoxMatchResult.LazySimpleMatch) return@p true // 直接认为是匹配的
            if (it.result is ParadoxMatchResult.PartialMatch) return@p false // 之后再匹配
            if (it.result is ParadoxMatchResult.FallbackMatch) return@p false // 之后再匹配
            it.result.get(options)
        }
        if (matched.isNotEmpty()) return matched.mapFast { it.value }

        candidates.filterTo(matched) { it.result is ParadoxMatchResult.PartialMatch }
        if (matched.isNotEmpty()) return matched.mapFast { it.value }

        candidates.filterTo(matched) { it.result is ParadoxMatchResult.FallbackMatch }
        if (matched.isNotEmpty()) return matched.mapFast { it.value }

        return emptyList()
    }

    private fun addLazyMatchedConfigs(
        matched: MutableList<ParadoxMatchCandidate>,
        candidates: List<ParadoxMatchCandidate>,
        options: ParadoxMatchOptions? = null,
        predicate: (ParadoxMatchCandidate) -> Boolean
    ) {
        val lazyMatched = candidates.filterFast(predicate)
        val lazyMatchedSize = lazyMatched.size
        if (lazyMatchedSize == 1) {
            matched += lazyMatched.first()
        } else if (lazyMatchedSize > 1) {
            val oldMatchedSize = matched.size
            lazyMatched.filterTo(matched) { it.result.get(options) }
            if (oldMatchedSize == matched.size) matched += lazyMatched.first()
        }
    }

    @Optimized
    fun optimize(
        element: PsiElement,
        expression: ParadoxScriptExpression,
        configs: List<CwtMemberConfig<*>>,
        options: ParadoxMatchOptions? = null
    ): List<CwtMemberConfig<*>> {
        // 进行后续优化

        if (configs.isEmpty()) return emptyList()
        val configGroup = configs.first().configGroup
        var result = configs
        var dynamic = false

        val context = ParadoxScriptExpressionMatchOptimizer.Context(element, expression, configGroup, options)
        val optimizers = ParadoxScriptExpressionMatchOptimizer.EP_NAME.extensionList
        optimizers.forEachFast f@{ optimizer ->
            val optimized = optimizer.optimize(result, context)
            if (optimized == null) return@f
            if (optimizer.isDynamic(context)) dynamic = true
            result = optimized
        }

        // NOTE 2.1.2 如果是动态的优化器，需要把正在解析的规则上下文标记为动态的
        if (dynamic) ParadoxConfigService.getResolvingConfigContext()?.dynamic = true

        return result
    }
}
