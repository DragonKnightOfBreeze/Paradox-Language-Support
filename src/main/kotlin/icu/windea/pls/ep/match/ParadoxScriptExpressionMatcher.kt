package icu.windea.pls.ep.match

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.match.ParadoxPatternMatchService
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 用于匹配脚本表达式与规则表达式。
 *
 * @see ParadoxMatchService
 * @see ParadoxScriptExpressionElement
 * @see ParadoxScriptExpression
 * @see CwtDataExpression
 */
interface ParadoxScriptExpressionMatcher {
    /**
     * 是否支持将规则表达式作为通配符，然后进行匹配。
     *
     * @see ParadoxPatternMatchService
     */
    fun isPatternAware(): Boolean = false

    /**
     * 匹配脚本表达式和规则表达式。
     */
    fun match(context: Context): ParadoxMatchResult?

    /**
     * 匹配上下文。
     *
     * @property element 上下文 PSI 元素。
     * @property expression 脚本表达式。
     * @property configExpression 规则表达式。
     * @property config 上下文规则。
     * @property configGroup 规则分组。
     */
    data class Context(
        val element: PsiElement,
        val expression: ParadoxScriptExpression,
        val configExpression: CwtDataExpression,
        val config: CwtConfig<*>?,
        val configGroup: CwtConfigGroup,
        val options: Int = ParadoxMatchOptions.Default,
    ) {
        val dataType get() = configExpression.type
        val project get() = configGroup.project
    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxScriptExpressionMatcher>("icu.windea.pls.scriptExpressionMatcher")
    }
}
