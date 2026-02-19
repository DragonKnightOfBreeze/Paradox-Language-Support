package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.ep.match.ParadoxCsvExpressionMatcher
import icu.windea.pls.ep.match.ParadoxScriptExpressionMatchOptimizer
import icu.windea.pls.ep.match.ParadoxScriptExpressionMatcher
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression

/**
 * 脚本表达式的匹配上下文。
 *
 * @property element 上下文 PSI 元素。
 * @property expression 脚本表达式。
 * @property configExpression 规则表达式。
 * @property config 上下文规则。
 * @property configGroup 规则分组。
 *
 * @see ParadoxScriptExpressionMatcher
 */
data class ParadoxScriptExpressionMatchContext(
    val element: PsiElement,
    val expression: ParadoxScriptExpression,
    val configExpression: CwtDataExpression,
    val config: CwtConfig<*>?,
    val configGroup: CwtConfigGroup,
    val options: ParadoxMatchOptions? = null,
) {
    val dataType get() = configExpression.type
    val project get() = configGroup.project
    val gameType get() = configGroup.gameType
}
/**
 * 脚本表达式的优化上下文。
 *
 * @property element 上下文 PSI 元素。
 * @property expression 脚本表达式。
 * @property configGroup 规则分组。
 *
 * @see ParadoxScriptExpressionMatchOptimizer
 */
data class ParadoxScriptExpressionMatchOptimizerContext(
    val element: PsiElement,
    val expression: ParadoxScriptExpression,
    val configGroup: CwtConfigGroup,
    val options: ParadoxMatchOptions? = null,
) {
    val project get() = configGroup.project
    val gameType get() = configGroup.gameType
}

/**
 * CSV 表达式的匹配上下文。
 *
 * @property element 上下文 PSI 元素。
 * @property expressionText 表达式文本。
 * @property configExpression 规则表达式。
 * @property configGroup 规则分组。
 *
 * @see ParadoxCsvExpressionMatcher
 */
data class ParadoxCsvExpressionMatchContext(
    val element: PsiElement,
    val expressionText: String,
    val configExpression: CwtDataExpression,
    val configGroup: CwtConfigGroup,
) {
    val dataType get() = configExpression.type
    val project get() = configGroup.project
    val gameType get() = configGroup.gameType
}
