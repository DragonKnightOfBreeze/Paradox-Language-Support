package icu.windea.pls.ep.match

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.match.ParadoxMatchPipeline
import icu.windea.pls.lang.match.ParadoxScriptExpressionMatchOptimizerContext
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 用于优化脚本表达式与规则表达式的优化逻辑。
 *
 * @see ParadoxMatchPipeline
 * @see ParadoxScriptExpressionElement
 * @see ParadoxScriptExpression
 * @see CwtMemberConfig
 */
interface ParadoxScriptExpressionMatchOptimizer {
    /**
     * 优化逻辑是否依赖脚本文件中的上下文。
     */
    fun isDynamic(context: ParadoxScriptExpressionMatchOptimizerContext): Boolean = false

    /**
     * 按匹配结果过滤了候选的一组成员规则后，进行后续优化。
     *
     * @return 优化后的候选规则列表。如果为 `null`，则表示此扩展点不适用。
     */
    fun optimize(configs: List<CwtMemberConfig<*>>, context: ParadoxScriptExpressionMatchOptimizerContext): List<CwtMemberConfig<*>>?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxScriptExpressionMatchOptimizer>("icu.windea.pls.scriptExpressionMatchOptimizer")
    }
}
