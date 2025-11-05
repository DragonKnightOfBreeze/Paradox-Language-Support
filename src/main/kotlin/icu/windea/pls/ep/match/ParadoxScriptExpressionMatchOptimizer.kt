package icu.windea.pls.ep.match

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchPipeline
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
     * 按匹配结果过滤了候选的一组成员规则后，进行后续优化。
     */
    fun optimize(configs: List<CwtMemberConfig<*>>, context: Context): List<CwtMemberConfig<*>>

    /**
     * 优化上下文。
     *
     * @property element 上下文 PSI 元素。
     * @property expression 脚本表达式。
     * @property configGroup 规则分组。
     */
    data class Context(
        val element: PsiElement,
        val expression: ParadoxScriptExpression,
        val configGroup: CwtConfigGroup,
        val options: Int = ParadoxMatchOptions.Default,
    ) {
        val project get() = configGroup.project
    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxScriptExpressionMatchOptimizer>("icu.windea.pls.scriptExpressionMatchOptimizer")
    }
}
