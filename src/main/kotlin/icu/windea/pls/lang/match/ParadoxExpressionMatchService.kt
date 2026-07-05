package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.processUnionCandidates
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.ep.match.expression.ParadoxCsvExpressionMatcher
import icu.windea.pls.ep.match.expression.ParadoxScriptExpressionMatcher
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.model.type.ParadoxExpressionRole

object ParadoxExpressionMatchService {
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

    fun matchesConstant(expression: ParadoxExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Boolean {
        // 注意这里可能需要在同一循环中同时检查 keyExpression 和 valueExpression，因此这里需要特殊处理
        if (!matchesExpressionRole(expression, configExpression)) return false

        return when (configExpression.type) {
            CwtDataTypes.Constant -> true
            CwtDataTypes.EnumValue -> {
                val enumName = configExpression.value ?: return false
                val enumConfig = configGroup.enums[enumName] ?: return false
                enumConfig.values.contains(expression.value)
            }
            CwtDataTypes.UnionValue -> {
                val unionName = configExpression.value ?: return false
                val unionConfig = configGroup.unions[unionName] ?: return false
                unionConfig.processUnionCandidates { valueConfig ->
                    if (matchesConstant(expression, valueConfig.configExpression, configGroup)) return true
                    true
                }
                false
            }
            CwtDataTypes.Value, CwtDataTypes.DynamicValue -> {
                val type = configExpression.value ?: return false
                val dynamicValueConfig = configGroup.dynamicValueTypes[type] ?: return false
                dynamicValueConfig.values.contains(expression.value)
            }
            else -> false
        }
    }

    fun matchesExpressionRole(expression: ParadoxExpression, configExpression: CwtDataExpression): Boolean {
        return when (expression.role) {
            ParadoxExpressionRole.Key -> configExpression.isKey
            ParadoxExpressionRole.Value -> !configExpression.isKey
            else -> true
        }
    }

    fun getMatchedScriptUnionCandidate(element: PsiElement, expression: ParadoxExpression, unionName: String, configGroup: CwtConfigGroup, options: ParadoxMatchOptions? = null): CwtValueConfig? {
        val unionConfig = configGroup.unions[unionName] ?: return null
        unionConfig.processUnionCandidates { valueConfig ->
            val configExpression = valueConfig.configExpression
            val context = ParadoxScriptExpressionMatchContext(element, expression, configExpression, valueConfig, configGroup, options)
            if (matchScriptExpression(context).get(options)) return valueConfig
            true
        }
        return null
    }

    fun getMatchedCsvUnionCandidate(element: PsiElement, expression: ParadoxExpression, unionName: String, configGroup: CwtConfigGroup): CwtValueConfig? {
        val unionConfig = configGroup.unions[unionName] ?: return null
        unionConfig.processUnionCandidates { valueConfig ->
            val configExpression = valueConfig.configExpression
            val context = ParadoxCsvExpressionMatchContext(element, expression, configExpression, configGroup)
            if (matchCsvExpression(context).get()) return valueConfig
            true
        }
        return null
    }

    fun getMatchedAliasKey(element: PsiElement, expression: ParadoxExpression, aliasName: String, configGroup: CwtConfigGroup, options: ParadoxMatchOptions? = null): String? {
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
}
