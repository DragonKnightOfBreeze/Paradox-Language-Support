package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.ep.match.ParadoxCsvExpressionMatcher
import icu.windea.pls.ep.match.ParadoxScriptExpressionMatcher
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression

object ParadoxMatchService {
    /**
     * @see ParadoxScriptExpressionMatcher.match
     */
    fun matchScriptExpression(
        element: PsiElement,
        expression: ParadoxScriptExpression,
        configExpression: CwtDataExpression,
        config: CwtConfig<*>?,
        configGroup: CwtConfigGroup,
        options: Int = ParadoxMatchOptions.Default
    ): ParadoxMatchResult {
        val matchContext = ParadoxScriptExpressionMatcher.Context(element, expression, configExpression, config, configGroup, options)
        val matchResult = ParadoxScriptExpressionMatcher.EP_NAME.extensionList.firstNotNullOfOrNull { ep -> ep.match(matchContext) }
        return matchResult ?: ParadoxMatchResult.NotMatch
    }

    /**
     * @see ParadoxCsvExpressionMatcher.match
     */
    fun matchCsvExpression(
        element: PsiElement,
        expressionText: String,
        configExpression: CwtDataExpression,
        configGroup: CwtConfigGroup,
    ): ParadoxMatchResult {
        val matchContext = ParadoxCsvExpressionMatcher.Context(element, expressionText, configExpression, configGroup)
        val matchResult = ParadoxCsvExpressionMatcher.EP_NAME.extensionList.firstNotNullOfOrNull { ep -> ep.match(matchContext) }
        return matchResult ?: ParadoxMatchResult.NotMatch
    }
}
