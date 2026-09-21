package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.SmartList
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.configGroup.mockConfigModel
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.config.match.CwtConfigMatchService
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.lang.manipulation.ParadoxConfigManipulationService

@Optimized
object ParadoxMatchCandidateService {
    fun collect(context: ParadoxExpressionMatchContext, configs: List<CwtMemberConfig<*>>, forValue: Boolean): List<ParadoxMatchCandidate> {
        if (configs.isEmpty()) return emptyList()
        val result = SmartList<ParadoxMatchCandidate>() // 3.0.1 optimize: use `SmartList` (0 or 1 elements in most situations)
        configs.forEachFast { config ->
            collectInternal(context, forValue, result, config)
        }
        return result
    }

    private fun collectInternal(context: ParadoxExpressionMatchContext, forValue: Boolean, collected: MutableList<ParadoxMatchCandidate>, config: CwtMemberConfig<*>) {
        if (CwtConfigMatchService.isAliasEntry(config)) {
            // NOTE 3.0.3 inline alias entry before further match
            collectFromAliasEntry(context, forValue, collected, config)
            return
        }
        collectMatched(context, forValue, collected, config)
    }

    private fun collectFromAliasEntry(context: ParadoxExpressionMatchContext, forValue: Boolean, collected: MutableList<ParadoxMatchCandidate>, config: CwtMemberConfig<*>) {
        if (forValue) return
        if (config !is CwtPropertyConfig) return
        val configGroup = context.configGroup
        val aliasName = config.configExpression.metadata.value ?: return
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return
        // NOTE 3.0.3 recursion guard is required here
        val map = mutableMapOf<String, ParadoxMatchResult>()
        runWithRecursionGuard("matchCandidate.collectFromAliasEntry", aliasName) {
            ParadoxConfigManipulationService.expandAndMatchAliasKeys(context.element, context.expression, aliasName, configGroup, context.options) p@{ key, matchResult ->
                map.put(key, matchResult)
                true
            }
        }
        if (map.isEmpty()) return
        ProgressManager.checkCanceled()
        val result = SmartList<ParadoxMatchCandidate>() // 3.0.3 optimize: use `SmartList` (0 or 1 elements in most situations)
        map.forEach f1@{ (key, matchResult) ->
            val aliasConfigs = aliasGroup[key]
            aliasConfigs?.forEachFast f2@{ aliasConfig ->
                val inlined = CwtConfigManipulationService.inlineAlias(config, aliasConfig) ?: return@f2
                if (result.size >= 16) {
                    result.clear()
                    return@f1 // too many candidates, break
                }
                val candidate = ParadoxMatchCandidate(inlined, matchResult)
                result += candidate
            }
        }
        if (result.isEmpty()) { // too many candidates, use fallback config (`$any = $any`)
            collected.clear()
            val fallbackConfig = context.configGroup.mockConfigModel.anyProperty
            val fallbackCandidate = ParadoxMatchCandidate(fallbackConfig, ParadoxMatchResult.FallbackMatch)
            collected.add(fallbackCandidate)
            return
        }
        collected.addAll(result)

        // NOTE 3.0.3 cannot apply injection for alias keys (e.g. `y` in `alias[x:y]`) - unsupported from now on
    }

    private fun collectMatched(context: ParadoxExpressionMatchContext, forValue: Boolean, collected: MutableList<ParadoxMatchCandidate>, config: CwtMemberConfig<*>) {
        val configExpression = if (forValue) config.valueExpression else config.configExpression
        val matchResult = ParadoxExpressionMatchService.matchScriptExpression(context, configExpression, config)
        if (matchResult === ParadoxMatchResult.NotMatch) return
        val candidate = ParadoxMatchCandidate(config, matchResult)
        collected += candidate
    }

    fun process(context: ParadoxExpressionMatchContext, candidates: List<ParadoxMatchCandidate>): List<ParadoxMatchCandidate> {
        if (candidates.isEmpty()) return emptyList()
        val result = SmartList<ParadoxMatchCandidate>() // 3.0.1 optimize: use `SmartList` (0 or 1 elements in most situations)
        processInternal(context, candidates, result)
        return result
    }

    private fun processInternal(context: ParadoxExpressionMatchContext, candidates: List<ParadoxMatchCandidate>, matched: MutableList<ParadoxMatchCandidate>) {
        // 步骤：
        // - 处理精确匹配（`ExactMatch` `LenientExactMatch`），如果有结果，则仅使用这些结果，并直接返回
        // - 处理需要检测子句内容的匹配（`LazyBlockAwareMatch`），如果存在匹配项，则保留所有匹配项或者第一个候选项
        // - 处理需要检测作用域上下文的匹配（`LazyScopeAwareMatch`），如果存在匹配项，则保留所有匹配项或者第一个候选项
        // - 处理其余的各种直接匹配（`DirectMatch`），如果有结果，则仅使用这些结果
        // - 处理通配符匹配（`WildcardMatch`，不验证表达式自身或其中某部分在解析引用后是否合法），如果有结果，则仅使用这些结果
        // - 处理更宽松的通配符匹配（`LenientWildcardMatch`，这意味着存在另一种更精确的格式），如果有结果，则仅使用这些结果
        // - 处理部分匹配（`PartialMatch`），如果有结果，则仅使用这些结果
        // - 处理回退匹配（`FallbackMatch`），如果有结果，则仅使用这些结果
        // - 如果不是直接返回的情况，还需要处理带参数的匹配（`ParameterizedMatch`），如果有结果，则需要加入最终的结果中

        processUnchecked(candidates, matched) { it.result is ParadoxMatchResult.ExactMatch || it.result is ParadoxMatchResult.LenientExactMatch }
        if (matched.isNotEmpty()) return

        processMain(context, candidates, matched)
        processUnchecked(candidates, matched) { it.result is ParadoxMatchResult.ParameterizedMatch }
    }

    private fun processMain(context: ParadoxExpressionMatchContext, candidates: List<ParadoxMatchCandidate>, matched: MutableList<ParadoxMatchCandidate>) {
        processLenientChecked(context, candidates, matched) { it.result is ParadoxMatchResult.LazyBlockAwareMatch }
        processLenientChecked(context, candidates, matched) { it.result is ParadoxMatchResult.LazyScopeAwareMatch }

        processChecked(context, candidates, matched) { it.result is ParadoxMatchResult.DirectMatch }
        if (matched.isNotEmpty()) return

        processChecked(context, candidates, matched) { it.result === ParadoxMatchResult.WildcardMatch }
        if (matched.isNotEmpty()) return
        processChecked(context, candidates, matched) { it.result === ParadoxMatchResult.LenientWildcardMatch }
        if (matched.isNotEmpty()) return
        processChecked(context, candidates, matched) { it.result === ParadoxMatchResult.PartialMatch }
        if (matched.isNotEmpty()) return

        processChecked(context, candidates, matched) { it.result === ParadoxMatchResult.FallbackMatch }
    }

    private inline fun processChecked(context: ParadoxExpressionMatchContext, candidates: List<ParadoxMatchCandidate>, matched: MutableList<ParadoxMatchCandidate>, predicate: (ParadoxMatchCandidate) -> Boolean) {
        candidates.forEachFast f@{
            if (it.processed) return@f
            if (!predicate(it)) return@f
            it.processed = true
            if (!it.result.get(context.options)) return@f
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

    private inline fun processLenientChecked(context: ParadoxExpressionMatchContext, candidates: List<ParadoxMatchCandidate>, matched: MutableList<ParadoxMatchCandidate>, predicate: (ParadoxMatchCandidate) -> Boolean) {
        val lazyMatched = SmartList<ParadoxMatchCandidate>() // 3.0.1 optimize: use `SmartList` (0 or 1 elements in most situations)
        processUnchecked(candidates, lazyMatched, predicate)
        val lazyMatchedSize = lazyMatched.size
        if (lazyMatchedSize == 1) {
            matched += lazyMatched.first()
        } else if (lazyMatchedSize > 1) {
            val oldMatchedSize = matched.size
            lazyMatched.forEachFast f@{
                if (!it.result.get(context.options)) return@f
                matched += it
            }
            if (oldMatchedSize == matched.size) matched += lazyMatched.first()
        }
    }
}
