package icu.windea.pls.ep.match

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchService

/**
 * 用于匹配 CSV 表达式与规则表达式。
 *
 * @see ParadoxMatchService
 * @see ParadoxCsvExpressionElement
 * @see CwtDataExpression
 */
interface ParadoxCsvExpressionMatcher {
    /**
     * 匹配 CSV 表达式和规则表达式。
     */
    fun match(context: Context): ParadoxMatchResult?

    /**
     * 匹配上下文。
     *
     * @property element 上下文 PSI 元素。
     * @property expressionText 表达式文本。
     * @property configExpression 规则表达式。
     * @property configGroup 规则分组。
     */
    data class Context(
        val element: PsiElement,
        val expressionText: String,
        val configExpression: CwtDataExpression,
        val configGroup: CwtConfigGroup,
    ) {
        val dataType get() = configExpression.type
        val project get() = configGroup.project
    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxCsvExpressionMatcher>("icu.windea.pls.csvExpressionMatcher")
    }
}
