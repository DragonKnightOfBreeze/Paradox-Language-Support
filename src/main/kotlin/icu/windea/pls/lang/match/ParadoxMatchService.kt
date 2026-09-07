package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.model.expressions.ParadoxExpression

@Optimized
object ParadoxMatchService {
    // region Predicates

    fun isDumb(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.skipIndex || options.skipScope || ParadoxMatchOptions.isDumb()
    }

    fun lenient(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.lenient
    }

    fun forExpression(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.forExpression
    }

    fun forDeclarationRoot(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.forDeclarationRoot
    }

    fun skipBlock(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.skipBlock
    }

    fun skipIndex(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.skipIndex || ParadoxMatchOptions.isDumb()
    }

    fun skipScope(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.skipScope || ParadoxMatchOptions.isDumb()
    }

    // endregion

    /**
     * 根据来自 [matchResultProvider] 的匹配结果，从输入的一组成员规则 [configs] 收集匹配候选项。
     */
    inline fun <T : CwtMemberConfig<*>> collectCandidates(configs: List<T>, matchResultProvider: (T) -> ParadoxMatchResult): List<ParadoxMatchCandidate> {
        return ParadoxMatchCandidateService.collect(configs, matchResultProvider)
    }

    /**
     * 处理输入的一组匹配候选项 [candidates]，进行进一步的匹配。
     */
    fun processCandidates(candidates: List<ParadoxMatchCandidate>, options: ParadoxMatchOptions? = null): List<ParadoxMatchCandidate> {
        return ParadoxMatchCandidateService.process(candidates, options)
    }

    /**
     * 处理输入的一组待进一步匹配的规则 [configs]，进行后续优化。
     */
    fun <T : CwtMemberConfig<*>> optimize(configs: List<T>, element: PsiElement, expression: ParadoxExpression, options: ParadoxMatchOptions? = null): List<T> {
        if (configs.isEmpty()) return emptyList()
        val configGroup = configs.first().configGroup
        val context = ParadoxScriptExpressionMatchOptimizerContext(element, expression, configGroup, options)
        return ParadoxExpressionMatchService.optimizeScriptExpression(configs, context)
    }

    // endregion
}
