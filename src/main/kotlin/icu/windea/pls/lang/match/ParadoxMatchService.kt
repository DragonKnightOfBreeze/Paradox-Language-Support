package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import com.intellij.util.SmartList
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.ep.match.expression.ParadoxScriptExpressionMatchOptimizer
import icu.windea.pls.model.expressions.ParadoxExpression

@Optimized
object ParadoxMatchService {
    // region Predicates

    fun isDumb(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.skipIndex || options.skipScope || processingMergedIndex()
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
        return options.skipIndex || processingMergedIndex()
    }

    fun skipScope(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.skipScope || processingMergedIndex()
    }

    private fun processingMergedIndex() = ChronicleThreadContext.processMergedIndex.get() == true

    // endregion

    // region Pipeline Methods

    /**
     * 根据来自 [matchResultProvider] 的匹配结果，从输入的一组成员规则 [configs] 收集匹配候选项。
     */
    @Optimized
    inline fun <T : CwtMemberConfig<*>> collectCandidates(configs: List<T>, matchResultProvider: (T) -> ParadoxMatchResult): List<ParadoxMatchCandidate> {
        if (configs.isEmpty()) return emptyList()
        val result = SmartList<ParadoxMatchCandidate>() // 3.0.1 optimize: use SmartList (0 or 1 elements in most situations)
        configs.forEachFast f@{ config ->
            val matchResult = matchResultProvider(config)
            if (matchResult == ParadoxMatchResult.NotMatch) return@f
            val matchCandidate = ParadoxMatchCandidate(config, matchResult)
            result += matchCandidate
        }
        return result
    }

    /**
     * 处理输入的一组匹配候选项 [candidates]，进行进一步的匹配。基于匹配结果的类型。
     */
    @Optimized
    fun process(candidates: List<ParadoxMatchCandidate>, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        if (candidates.isEmpty()) return emptyList()
        val matched = ParadoxMatchProcessor.process(candidates, options)
        return matched.mapFast { it.value }
    }

    /**
     * 对输入的一组已处理过的成员规则 [configs] 进行后续优化。基于 [ParadoxScriptExpressionMatchOptimizer]。
     */
    @Optimized
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
        if (dynamic) getResolvingConfigContext()?.dynamic = true

        return result
    }

    private fun getResolvingConfigContext() = ChronicleThreadContext.resolvingConfigContextStack.get()?.peekLast()

    // endregion
}
