package icu.windea.pls.lang.match

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.annotations.Optimized

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

    // region Pipeline Methods

    /**
     * 根据匹配结果，从输入的一组成员规则 [configs] 收集匹配候选项。
     */
    fun collectCandidates(context: ParadoxExpressionMatchContext, configs: List<CwtMemberConfig<*>>, forValue: Boolean = false): List<ParadoxMatchCandidate> {
        return ParadoxMatchCandidateService.collect(context, configs, forValue)
    }

    /**
     * 处理输入的一组匹配候选项 [candidates]，进行进一步的匹配。
     */
    fun processCandidates(context: ParadoxExpressionMatchContext, candidates: List<ParadoxMatchCandidate>): List<ParadoxMatchCandidate> {
        return ParadoxMatchCandidateService.process(context, candidates)
    }

    /**
     * 处理输入的一组待进一步匹配的规则 [configs]，进行后续优化。
     */
    fun <T : CwtMemberConfig<*>> optimize(context: ParadoxExpressionMatchContext, configs: List<T>): List<T> {
        if (configs.isEmpty()) return emptyList()
        return ParadoxExpressionMatchService.optimizeScriptExpression(context, configs)
    }

    // endregion
}
