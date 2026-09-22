package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.SmartList
import icu.windea.pls.base.ChronicleCapacities
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configGroup.mockConfigs
import icu.windea.pls.config.manipulation.CwtConfigInlineService
import icu.windea.pls.config.match.CwtConfigMatchService
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.processFast
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.lang.manipulation.ParadoxConfigExpansionService

@Optimized
object ParadoxMatchCandidateService {
    fun collect(context: ParadoxExpressionMatchContext, configs: List<CwtMemberConfig<*>>, forValue: Boolean): List<ParadoxMatchCandidate> {
        if (configs.isEmpty()) return emptyList()
        val result = SmartList<ParadoxMatchCandidate>() // 3.0.1 optimize: use `SmartList` (0 or 1 elements in most situations)
        collectInternal(configs, context, forValue, result)
        return result
    }

    private fun collectInternal(configs: List<CwtMemberConfig<*>>, context: ParadoxExpressionMatchContext, forValue: Boolean, result: SmartList<ParadoxMatchCandidate>): Boolean {
        return configs.processFast { config ->
            collectMatched(config, context, forValue, result)
        }
    }

    private fun collectMatched(config: CwtMemberConfig<*>, context: ParadoxExpressionMatchContext, forValue: Boolean, result: MutableList<ParadoxMatchCandidate>): Boolean {
        if (CwtConfigMatchService.isAliasEntry(config)) {
            // NOTE 3.0.3 inline alias entry before further match
            return collectFromAliasEntry(config, context, forValue, result)
        }
        return collectFromNormalEntry(config, context, forValue, result)
    }

    private fun collectFromAliasEntry(config: CwtMemberConfig<*>, context: ParadoxExpressionMatchContext, forValue: Boolean, result: MutableList<ParadoxMatchCandidate>): Boolean {
        if (forValue) return true
        if (config !is CwtPropertyConfig) return true
        val configGroup = context.configGroup
        val aliasName = config.configExpression.metadata.value ?: return true
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return true
        // NOTE 3.0.3 recursion guard is required here
        val map = mutableMapOf<String, ParadoxMatchResult>()
        runWithRecursionGuard("matchCandidate.collectFromAliasEntry", aliasName) {
            ParadoxConfigExpansionService.expandAndMatchAliasKeys(context.element, context.expression, aliasName, configGroup, context.options) p@{ key, matchResult ->
                map.put(key, matchResult)
                true
            }
        }
        if (map.isEmpty()) return true
        ProgressManager.checkCanceled()
        map.forEach f1@{ (key, matchResult) ->
            val aliasConfigs = aliasGroup[key]
            aliasConfigs?.forEachFast f2@{ aliasConfig ->
                val inlined = CwtConfigInlineService.inlineAlias(config, aliasConfig) ?: return@f2
                val inlinedCandidate = ParadoxMatchCandidate(inlined, matchResult)
                collectCandidate(inlinedCandidate, result).let { if (!it) return false }
            }
        }
        // NOTE 3.0.3 cannot apply injection for alias keys (e.g. `y` in `alias[x:y]`) - unsupported from now on
        return true
    }

    private fun collectFromNormalEntry(config: CwtMemberConfig<*>, context: ParadoxExpressionMatchContext, forValue: Boolean, result: MutableList<ParadoxMatchCandidate>): Boolean {
        val configExpression = if (forValue) config.valueExpression else config.configExpression
        val matchResult = ParadoxExpressionMatchService.matchScriptExpression(context, configExpression, config)
        if (matchResult === ParadoxMatchResult.NotMatch) return true
        val candidate = ParadoxMatchCandidate(config, matchResult)
        return collectCandidate(candidate, result)
    }

    private fun collectCandidate(candidate: ParadoxMatchCandidate, result: MutableList<ParadoxMatchCandidate>): Boolean {
        if (result.size >= ChronicleCapacities.maxMatchCandidateSize()) {
            // NOTE 3.0.3 too many candidates, use fallback match with any data type (clear all collected candidates first)
            val mockConfigs = candidate.value.configGroup.mockConfigs
            val fallbackConfig = when (candidate.value) {
                is CwtPropertyConfig -> mockConfigs.anyProperty
                is CwtValueConfig -> mockConfigs.anyValue
            }
            result.clear()
            val fallbackCandidate = ParadoxMatchCandidate(fallbackConfig, ParadoxMatchResult.FallbackMatch)
            result += fallbackCandidate
            return false
        }
        result += candidate
        return true
    }

    fun process(context: ParadoxExpressionMatchContext, candidates: List<ParadoxMatchCandidate>): List<ParadoxMatchCandidate> {
        if (candidates.isEmpty()) return emptyList()
        val result = SmartList<ParadoxMatchCandidate>() // 3.0.1 optimize: use `SmartList` (0 or 1 elements in most situations)
        processInternal(context, candidates, result)
        return result
    }

    private fun processInternal(context: ParadoxExpressionMatchContext, candidates: List<ParadoxMatchCandidate>, result: MutableList<ParadoxMatchCandidate>): Boolean {
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

        processUnchecked(candidates, result) { c -> c.result is ParadoxMatchResult.ExactMatch || c.result is ParadoxMatchResult.LenientExactMatch }.let { if (!it) return false }
        if (result.isNotEmpty()) return true

        processMain(context, candidates, result).let { if (!it) return false }
        processUnchecked(candidates, result) { c -> c.result is ParadoxMatchResult.ParameterizedMatch }.let { if (!it) return false }

        return true
    }

    private fun processMain(context: ParadoxExpressionMatchContext, candidates: List<ParadoxMatchCandidate>, result: MutableList<ParadoxMatchCandidate>): Boolean {
        processLenientChecked(context, candidates, result) { c -> c.result is ParadoxMatchResult.LazyBlockAwareMatch }.let { if (!it) return false }
        processLenientChecked(context, candidates, result) { it.result is ParadoxMatchResult.LazyScopeAwareMatch }.let { if (!it) return false }

        processChecked(context, candidates, result) { c -> c.result is ParadoxMatchResult.DirectMatch }.let { if (!it) return false }
        if (result.isNotEmpty()) return true

        processChecked(context, candidates, result) { c -> c.result === ParadoxMatchResult.WildcardMatch }.let { if (!it) return false }
        if (result.isNotEmpty()) return true
        processChecked(context, candidates, result) { c -> c.result === ParadoxMatchResult.LenientWildcardMatch }.let { if (!it) return false }
        if (result.isNotEmpty()) return true
        processChecked(context, candidates, result) { c -> c.result === ParadoxMatchResult.PartialMatch }.let { if (!it) return false }
        if (result.isNotEmpty()) return true

        processChecked(context, candidates, result) { c -> c.result === ParadoxMatchResult.FallbackMatch }.let { if (!it) return false }

        return true
    }

    private inline fun processChecked(context: ParadoxExpressionMatchContext, candidates: List<ParadoxMatchCandidate>, result: MutableList<ParadoxMatchCandidate>, predicate: (ParadoxMatchCandidate) -> Boolean): Boolean {
        return candidates.processFast p@{ c ->
            if (c.processed) return@p true
            if (!predicate(c)) return@p true
            c.processed = true
            if (!c.result.get(context.options)) return@p true
            collectProcessedCandidate(c, result)
        }
    }

    private inline fun processUnchecked(candidates: List<ParadoxMatchCandidate>, result: MutableList<ParadoxMatchCandidate>, predicate: (ParadoxMatchCandidate) -> Boolean): Boolean {
        return candidates.processFast p@{ c ->
            if (c.processed) return@p true
            if (!predicate(c)) return@p true
            c.processed = true
            collectProcessedCandidate(c, result)
        }
    }

    private inline fun processLenientChecked(context: ParadoxExpressionMatchContext, candidates: List<ParadoxMatchCandidate>, result: MutableList<ParadoxMatchCandidate>, predicate: (ParadoxMatchCandidate) -> Boolean): Boolean {
        val lazyMatched = SmartList<ParadoxMatchCandidate>() // 3.0.1 optimize: use `SmartList` (0 or 1 elements in most situations)
        processUnchecked(candidates, lazyMatched, predicate)
        val lazyMatchedSize = lazyMatched.size
        if (lazyMatchedSize == 1) {
            collectProcessedCandidate(lazyMatched.first(), result).let { if (!it) return false }
        } else if (lazyMatchedSize > 1) {
            val oldMatchedSize = result.size
            lazyMatched.forEachFast f@{ c ->
                if (!c.result.get(context.options)) return@f
                collectProcessedCandidate(c, result).let { if (!it) return false }
            }
            if (oldMatchedSize == result.size) {
                collectProcessedCandidate(lazyMatched.first(), result).let { if (!it) return false }
            }
        }
        return true
    }

    private fun collectProcessedCandidate(candidate: ParadoxMatchCandidate, result: MutableList<ParadoxMatchCandidate>): Boolean {
        if (result.size >= ChronicleCapacities.maxProcessedMatchCandidateSize()) {
            // NOTE 3.0.3 too many candidates, use fallback match with any data type (clear all collected candidates first)
            val mockConfigs = candidate.value.configGroup.mockConfigs
            val fallbackConfig = when (candidate.value) {
                is CwtPropertyConfig -> mockConfigs.anyProperty
                is CwtValueConfig -> mockConfigs.anyValue
            }
            result.clear()
            val fallbackCandidate = ParadoxMatchCandidate(fallbackConfig, ParadoxMatchResult.FallbackMatch)
            result += fallbackCandidate
            return false
        }
        result += candidate
        return true
    }
}
