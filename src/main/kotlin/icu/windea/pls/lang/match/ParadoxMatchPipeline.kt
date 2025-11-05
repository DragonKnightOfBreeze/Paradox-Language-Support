package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.ep.config.CwtOverriddenConfigProvider
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager.isConstantMatch
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.script.psi.ParadoxScriptProperty

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
        if (configs.isEmpty()) return emptyList()
        val configGroup = configs.first().configGroup
        var result = configs

        run r1@{
            // 如果要匹配的是字符串，且匹配结果中存在作为常量匹配的规则，则仅保留这些规则
            if (result.size <= 1) return@r1
            if (expression.type != ParadoxType.String) return@r1
            val result1 = result.filter { isConstantMatch(configGroup, expression, it.configExpression) }
            if (result1.isEmpty()) return@r1
            result = result1
        }

        run r1@{
            // 如果匹配结果中存在键相同的规则，且其值是子句，则尝试根据子句进行进一步的匹配
            if (result.isEmpty()) return@r1
            val group = result.filterIsInstance<CwtPropertyConfig>().groupBy { it.key }.values
            if (group.isEmpty()) return@r1
            val filteredGroup = group.filter { configs -> configs.size > 1 && configs.filter { it.valueType == CwtType.Block }.size > 1 }
            if (filteredGroup.isEmpty()) return@r1
            val blockElement = element.castOrNull<ParadoxScriptProperty>()?.block ?: return@r1
            val blockExpression = ParadoxScriptExpression.resolveBlock()
            val configsToRemove = mutableSetOf<CwtPropertyConfig>()
            for (configs in filteredGroup) {
                for (config in configs) {
                    val valueConfig = config.valueConfig ?: continue
                    val matchResult = ParadoxMatchService.matchScriptExpression(blockElement, blockExpression, valueConfig.configExpression, valueConfig, configGroup, matchOptions)
                    if (matchResult.get(matchOptions)) continue
                    configsToRemove += config
                }
            }
            if (configsToRemove.isEmpty()) return@r1
            val result1 = result.filter { it !in configsToRemove }
            result = result1
        }

        run r1@{
            // 如果结果不为空且结果中存在需要重载的规则，则全部替换成重载后的规则
            if (result.isEmpty()) return@r1
            val result1 = mutableListOf<CwtMemberConfig<*>>()
            for (config in result) {
                val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(element, config)
                if (overriddenConfigs.isEmpty()) {
                    result1 += config
                    continue
                }
                // 这里需要再次进行匹配
                for (c in overriddenConfigs) {
                    val matchResult = ParadoxMatchService.matchScriptExpression(element, expression, c.configExpression, c, configGroup, matchOptions)
                    if (matchResult.get(matchOptions)) {
                        result1 += c
                    }
                }
            }
            result = result1
        }

        return result
    }
}
