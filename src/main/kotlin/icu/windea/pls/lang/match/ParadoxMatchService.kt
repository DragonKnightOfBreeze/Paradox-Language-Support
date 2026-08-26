package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.hasState
import icu.windea.pls.ep.match.expression.ParadoxScriptExpressionMatchOptimizer
import icu.windea.pls.lang.ParadoxThreadContext
import icu.windea.pls.lang.index.ParadoxMergedIndexThreadContext
import icu.windea.pls.model.expressions.ParadoxExpression

@Optimized
object ParadoxMatchService {
    // region Predicates

    fun isDumb(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.skipIndex || options.skipScope || ParadoxMergedIndexThreadContext.isProcessing.hasState()
    }

    fun fallback(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.fallback
    }

    fun forDeclarationRoot(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.forDeclarationRoot
    }

    fun lenient(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.lenient
    }

    fun skipIndex(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.skipIndex || ParadoxMergedIndexThreadContext.isProcessing.hasState()
    }

    fun skipScope(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.skipScope || ParadoxMergedIndexThreadContext.isProcessing.hasState()
    }

    // endregion

    // region Pipeline Methods

    /**
     * 根据来自 [matchResultProvider] 的匹配结果，从输入的一组成员规则 [configs] 收集匹配候选项。
     */
    inline fun <T : CwtMemberConfig<*>> collectCandidates(configs: List<T>, matchResultProvider: (T) -> ParadoxMatchResult): List<ParadoxMatchCandidate> {
        val result = ParadoxMatchCandidateService.collect(configs, matchResultProvider)
        return result
    }

    /**
     * 处理输入的一组匹配候选项 [candidates]，进行进一步的匹配。
     */
    fun processCandidates(candidates: List<ParadoxMatchCandidate>, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        val result = ParadoxMatchCandidateService.process(candidates, options).mapFast { it.value }
        return result
    }

    /**
     * 处理输入的一组匹配候选项 [candidates]，进行进一步的匹配，接着再进行后续优化。
     */
    fun processAndOptimizeCandidates(candidates: List<ParadoxMatchCandidate>, element: PsiElement, expression: ParadoxExpression, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        val result = ParadoxMatchCandidateService.process(candidates, options).mapFast { it.value }
        return optimize(result, element, expression, options)
    }

    /**
     * @see ParadoxScriptExpressionMatchOptimizer
     */
    fun <T : CwtMemberConfig<*>> optimize(configs: List<T>, element: PsiElement, expression: ParadoxExpression, options: ParadoxMatchOptions? = null): List<T> {
        if (configs.isEmpty()) return emptyList()
        val configGroup = configs.first().configGroup
        var result = configs
        var dynamic = false

        val context = ParadoxScriptExpressionMatchOptimizerContext(element, expression, configGroup, options)
        val optimizers = ParadoxScriptExpressionMatchOptimizer.getAll()
        optimizers.forEachFast f@{ optimizer ->
            val optimized = optimizer.optimize(result, context)
            if (optimized == null) return@f
            if (optimizer.isDynamic(context)) dynamic = true
            result = optimized
        }

        // NOTE 2.1.2 如果是动态的优化器，需要把正在解析的规则上下文标记为动态的
        if (dynamic) ParadoxThreadContext.resolvingConfigContext?.markDynamic()

        return result
    }

    // endregion
}
