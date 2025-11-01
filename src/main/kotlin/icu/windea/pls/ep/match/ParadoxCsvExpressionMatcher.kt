package icu.windea.pls.ep.match

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchResultProvider

/**
 * 用于匹配 CSV 表达式与规则表达式。
 *
 * @see ParadoxCsvExpressionElement
 * @see CwtDataExpression
 */
interface ParadoxCsvExpressionMatcher {
    /**
     * 匹配 CSV 表达式和规则表达式。
     *
     * @param element 上下文 PSI 元素。
     * @param expressionText 表达式文本。
     * @param configExpression 规则表达式。
     * @param configGroup 规则分组。
     * @return 匹配结果。
     *
     * @see ParadoxMatchResult
     * @see ParadoxMatchResultProvider
     */
    fun match(
        element: PsiElement,
        expressionText: String,
        configExpression: CwtDataExpression,
        configGroup: CwtConfigGroup
    ): ParadoxMatchResult?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxCsvExpressionMatcher>("icu.windea.pls.csvExpressionMatcher")
    }
}
