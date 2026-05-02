package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.ep.match.ParadoxCsvExpressionMatcher
import icu.windea.pls.ep.match.ParadoxScriptExpressionMatcher
import icu.windea.pls.model.expressions.ParadoxScriptExpression

object ParadoxExpressionMatchService {
    // region Script Expression Related

    /**
     * @see ParadoxScriptExpressionMatcher.match
     */
    @Optimized
    fun matchScriptExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        ParadoxScriptExpressionMatcher.EP_NAME.extensionList.forEachFast { ep ->
            ProgressManager.checkCanceled()
            val r = ep.match(context)
            if (r != null) return r
        }
        return ParadoxMatchResult.NotMatch
    }

    fun isConstantMatch(expression: ParadoxScriptExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Boolean {
        // 注意这里可能需要在同一循环中同时检查 keyExpression 和 valueExpression，因此这里需要特殊处理
        if (configExpression.isKey && expression.isKey == false) return false
        if (!configExpression.isKey && expression.isKey == true) return false

        return when (configExpression.type) {
            CwtDataTypes.Constant -> true
            CwtDataTypes.EnumValue -> configExpression.value?.let { configGroup.enums[it]?.values?.contains(expression.value) } == true
            CwtDataTypes.Value -> configExpression.value?.let { configGroup.dynamicValueTypes[it]?.values?.contains(expression.value) } == true
            else -> false
        }
    }

    fun getMatchedAliasKey(element: PsiElement, expression: ParadoxScriptExpression, aliasName: String, configGroup: CwtConfigGroup, options: ParadoxMatchOptions? = null): String? {
        val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(expression.value) // 不区分大小写
        if (constKey != null) return constKey
        val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return null
        return keys.find { key ->
            ProgressManager.checkCanceled()
            val configExpression = CwtDataExpression.resolve(key, true)
            val context = ParadoxScriptExpressionMatchContext(element, expression, configExpression, null, configGroup, options)
            matchScriptExpression(context).get(options)
        }
    }

    // endregion

    // region Csv Expression Related

    /**
     * @see ParadoxCsvExpressionMatcher.match
     */
    @Optimized
    fun matchCsvExpression(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        ParadoxCsvExpressionMatcher.EP_NAME.extensionList.forEachFast { ep ->
            ProgressManager.checkCanceled()
            val r = ep.match(context)
            if (r != null) return r
        }
        return ParadoxMatchResult.NotMatch
    }

    // endregion
}
