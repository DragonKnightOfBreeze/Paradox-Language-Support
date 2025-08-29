package icu.windea.pls.ep.expression

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.util.ParadoxExpressionMatcher
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Result

/**
 * 用于匹配CSV表达式与CWT规则表达式。
 *
 * @see ParadoxCsvExpressionElement
 * @see CwtDataExpression
 */
interface ParadoxCsvExpressionMatcher {
    /**
     * 匹配CSV表达式和CWT规则表达式。
     *
     * @param element 上下文PSI元素。
     * @param expressionText 表达式文本。
     * @param configExpression CWT规则表达式。
     * @param configGroup 规则分组。
     * @return 匹配结果。
     * @see ParadoxExpressionMatcher
     * @see ParadoxExpressionMatcher.Result
     */
    fun matches(
        element: PsiElement,
        expressionText: String,
        configExpression: CwtDataExpression,
        configGroup: CwtConfigGroup
    ): Result?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxCsvExpressionMatcher>("icu.windea.pls.csvExpressionMatcher")

        /**
         * @see ParadoxCsvExpressionMatcher.matches
         */
        fun matches(
            element: PsiElement,
            expressionText: String,
            configExpression: CwtDataExpression,
            configGroup: CwtConfigGroup,
        ): Result {
            EP_NAME.extensionList.forEach f@{ ep ->
                val r = ep.matches(element, expressionText, configExpression, configGroup)
                if (r != null) return r
            }
            return Result.NotMatch
        }
    }
}
