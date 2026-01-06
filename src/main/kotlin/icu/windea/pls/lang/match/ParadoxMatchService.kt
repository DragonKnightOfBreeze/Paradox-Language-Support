package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
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
        matchOptions: Int = ParadoxMatchOptions.Default
    ): ParadoxMatchResult {
        val context = ParadoxScriptExpressionMatcher.Context(element, expression, configExpression, config, configGroup, matchOptions)
        val result = ParadoxScriptExpressionMatcher.EP_NAME.extensionList.firstNotNullOfOrNull { ep -> ep.match(context) }
        return result ?: ParadoxMatchResult.NotMatch
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
        val context = ParadoxCsvExpressionMatcher.Context(element, expressionText, configExpression, configGroup)
        val result = ParadoxCsvExpressionMatcher.EP_NAME.extensionList.firstNotNullOfOrNull { ep -> ep.match(context) }
        return result ?: ParadoxMatchResult.NotMatch
    }

    fun isConstantMatch(configGroup: CwtConfigGroup, expression: ParadoxScriptExpression, configExpression: CwtDataExpression): Boolean {
        // 注意这里可能需要在同一循环中同时检查 `keyExpression` 和 `valueExpression`，因此这里需要特殊处理
        if (configExpression.isKey && expression.isKey == false) return false
        if (!configExpression.isKey && expression.isKey == true) return false

        if (configExpression.type == CwtDataTypes.Constant) return true
        if (configExpression.type == CwtDataTypes.EnumValue && configExpression.value?.let { configGroup.enums[it]?.values?.contains(expression.value) } == true) return true
        if (configExpression.type == CwtDataTypes.Value && configExpression.value?.let { configGroup.dynamicValueTypes[it]?.values?.contains(expression.value) } == true) return true
        return false
    }
}
