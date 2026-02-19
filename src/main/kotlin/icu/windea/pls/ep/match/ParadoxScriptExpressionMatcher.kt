package icu.windea.pls.ep.match

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.match.ParadoxPatternMatchService
import icu.windea.pls.lang.match.ParadoxScriptExpressionMatchContext
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
    fun isPatternAware(context: ParadoxScriptExpressionMatchContext): Boolean = false

    /**
     * 匹配脚本表达式和规则表达式。
     */
    fun match(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxScriptExpressionMatcher>("icu.windea.pls.scriptExpressionMatcher")
    }
}
