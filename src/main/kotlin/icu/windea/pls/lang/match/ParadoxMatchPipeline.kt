package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.annotations.Optimized
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
        val matched = ParadoxMatchCandidateService.process(candidates, options)
        return matched.mapFast { it.value }
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

