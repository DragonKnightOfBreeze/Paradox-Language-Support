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
        ParadoxScriptExpressionMatcher.EP_NAME.extensionList.forEach f@{ ep ->
            val r = ep.match(element, expression, configExpression, config, configGroup, options)
            if (r != null) return r
        }
        return ParadoxMatchResult.NotMatch
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
        ParadoxCsvExpressionMatcher.EP_NAME.extensionList.forEach f@{ ep ->
            val r = ep.match(element, expressionText, configExpression, configGroup)
            if (r != null) return r
        }
        return ParadoxMatchResult.NotMatch
    }
}
