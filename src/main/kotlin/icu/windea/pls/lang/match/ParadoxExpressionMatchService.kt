package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.processCandidateConfigs
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.ep.match.expression.ParadoxCsvExpressionMatcher
import icu.windea.pls.ep.match.expression.ParadoxScriptExpressionMatcher
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.model.type.ParadoxExpressionRole

@Optimized
object ParadoxExpressionMatchService {
    /**
     * @see ParadoxScriptExpressionMatcher.match
     */
    fun matchScriptExpression(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult {
        // ProgressManager.checkCanceled() // 3.0.1 optimize: not here (before cache access or lazy match instead)
        val matchers = ParadoxScriptExpressionMatcher.get(context.dataType) // 3.0.1 optimize: use global cache (by data type)
        matchers.forEachFast { matcher ->
            matcher.match(context)?.let { return it }
        }
        return ParadoxMatchResult.NotMatch
    }

    /**
     * @see ParadoxCsvExpressionMatcher.match
     */
    fun matchCsvExpression(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        // ProgressManager.checkCanceled() // 3.0.1 optimize: not here (before cache access or lazy match instead)
        val matchers = ParadoxCsvExpressionMatcher.get(context.dataType) // 3.0.1 optimize: use global cache (by data type)
        matchers.forEachFast { matcher ->
            matcher.match(context)?.let { return it }
        }
        return ParadoxMatchResult.NotMatch
    }

    fun matchesConstant(expression: ParadoxExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Boolean {
        // 注意这里可能需要在同一循环中同时检查 keyExpression 和 valueExpression，因此这里需要特殊处理
        if (!matchesExpressionRole(expression, configExpression)) return false

        return when (configExpression.type) {
            CwtDataTypes.Constant -> true
            CwtDataTypes.EnumValue -> {
                val enumName = configExpression.metadata.value ?: return false
                val enumConfig = configGroup.enums[enumName] ?: return false
                enumConfig.values.contains(expression.value)
            }
            CwtDataTypes.UnionValue -> {
                val unionName = configExpression.metadata.value ?: return false
                val unionConfig = configGroup.unions[unionName] ?: return false
                // NOTE 3.0.1 recursion guard is required here
                runWithRecursionGuard("exprssion.matchesConstant", unionName) {
                    unionConfig.processCandidateConfigs { valueConfig ->
                        if (matchesConstant(expression, valueConfig.configExpression, configGroup)) return true
                        true
                    }
                }
                false
            }
            CwtDataTypes.Value, CwtDataTypes.DynamicValue -> {
                val type = configExpression.metadata.value ?: return false
                val dynamicValueConfig = configGroup.dynamicValueTypes[type] ?: return false
                dynamicValueConfig.values.contains(expression.value)
            }
            else -> false
        }
    }

    fun matchesExpressionRole(expression: ParadoxExpression, configExpression: CwtDataExpression): Boolean {
        return when (expression.role) {
            ParadoxExpressionRole.Key -> configExpression.role == CwtDataExpressionRole.Key
            ParadoxExpressionRole.Value -> configExpression.role == CwtDataExpressionRole.Value
            else -> true
        }
    }

    fun getMatchedScriptUnionCandidate(element: PsiElement, expression: ParadoxExpression, unionName: String, configGroup: CwtConfigGroup, options: ParadoxMatchOptions? = null): CwtValueConfig? {
        val unionConfig = configGroup.unions[unionName] ?: return null
        // NOTE 3.0.1 recursion guard is not directly required here
        unionConfig.processCandidateConfigs { valueConfig ->
            ProgressManager.checkCanceled()
            val configExpression = valueConfig.configExpression
            val context = ParadoxScriptExpressionMatchContext(element, expression, configExpression, valueConfig, configGroup, options)
            if (matchScriptExpression(context).get(options)) return valueConfig
            true
        }
        return null
    }

    fun getMatchedCsvUnionCandidate(element: PsiElement, expression: ParadoxExpression, unionName: String, configGroup: CwtConfigGroup): CwtValueConfig? {
        val unionConfig = configGroup.unions[unionName] ?: return null
        // NOTE 3.0.1 recursion guard is not directly required here
        unionConfig.processCandidateConfigs { valueConfig ->
            ProgressManager.checkCanceled()
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
            ProgressManager.checkCanceled() // check cancellation
            val configExpression = CwtDataExpression.resolve(key)
            val context = ParadoxScriptExpressionMatchContext(element, expression, configExpression, null, configGroup, options)
            matchScriptExpression(context).get(options)
        }
    }
}
