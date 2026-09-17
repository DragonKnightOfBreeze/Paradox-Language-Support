package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.expandUnionCandidates
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigExpressionManager
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.util.ProcessorScope
import icu.windea.pls.ep.match.expression.ParadoxCsvExpressionMatcher
import icu.windea.pls.ep.match.expression.ParadoxScriptExpressionMatchOptimizer
import icu.windea.pls.ep.match.expression.ParadoxScriptExpressionMatcher
import icu.windea.pls.lang.ParadoxThreadContext
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.model.type.ParadoxExpressionRole

@Optimized
object ParadoxExpressionMatchService {
    /**
     * @see ParadoxScriptExpressionMatcher.match
     */
    fun matchScriptExpression(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        // ProgressManager.checkCanceled() // 3.0.1 optimize: not here (before cache access or lazy match instead)
        val matchers = ParadoxScriptExpressionMatcher.getAll(configExpression.type) // 3.0.1 optimize: use global cache (by data type)
        matchers.forEachFast { matcher ->
            matcher.match(context, configExpression, config)?.let { return it }
        }
        return ParadoxMatchResult.NotMatch
    }

    /**
     * @see ParadoxCsvExpressionMatcher.match
     */
    fun matchCsvExpression(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        // ProgressManager.checkCanceled() // 3.0.1 optimize: not here (before cache access or lazy match instead)
        val matchers = ParadoxCsvExpressionMatcher.getAll(configExpression.type) // 3.0.1 optimize: use global cache (by data type)
        matchers.forEachFast { matcher ->
            matcher.match(context, configExpression)?.let { return it }
        }
        return ParadoxMatchResult.NotMatch
    }

    /**
     * @see ParadoxScriptExpressionMatchOptimizer.optimize
     */
    fun <T : CwtMemberConfig<*>> optimizeScriptExpression(context: ParadoxExpressionMatchContext, input: List<T>): List<T> {
        var result = input
        var dynamic = false
        val optimizers = ParadoxScriptExpressionMatchOptimizer.getAll()
        optimizers.forEachFast f@{ optimizer ->
            val optimized = optimizer.optimize(context, result)
            if (optimized == null) return@f
            if (optimizer.isDynamic(context)) dynamic = true
            result = optimized
        }
        // NOTE 2.1.2 如果是动态的优化器，需要把正在解析的规则上下文标记为动态的
        if (dynamic) ParadoxThreadContext.resolvingConfigContext?.markDynamic()
        return result
    }

    fun matchesExpressionRole(expression: ParadoxExpression, configExpression: CwtDataExpression): Boolean {
        return when (expression.role) {
            ParadoxExpressionRole.Key -> configExpression.role == CwtDataExpressionRole.Key
            ParadoxExpressionRole.Value -> configExpression.role == CwtDataExpressionRole.Value
            else -> true
        }
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
                ProcessorScope.anyFrom {
                    runWithRecursionGuard("exprssion.matchesConstant", unionName) {
                        unionConfig.expandUnionCandidates { valueConfig ->
                            if (matchesConstant(expression, valueConfig.configExpression, configGroup)) process(valueConfig)
                            else true
                        }
                    }
                }
            }
            CwtDataTypes.Value, CwtDataTypes.DynamicValue -> {
                val type = configExpression.metadata.value ?: return false
                val dynamicValueConfig = configGroup.dynamicValueTypes[type] ?: return false
                dynamicValueConfig.values.contains(expression.value)
            }
            else -> false
        }
    }

    fun matchesTemplate(element: PsiElement, expression: ParadoxExpression, templateExpression: CwtTemplateExpression, configGroup: CwtConfigGroup, options: ParadoxMatchOptions? = null): Boolean {
        val snippetExpressions = templateExpression.snippetExpressions
        if (snippetExpressions.isEmpty()) return false
        val regex = CwtConfigExpressionManager.toRegex(templateExpression)
        val matchResult = regex.matchEntire(expression.value) ?: return false
        if (templateExpression.referenceExpressions.size != matchResult.groups.size - 1) return false
        var i = 1
        snippetExpressions.forEachFast f@{ snippetExpression ->
            ProgressManager.checkCanceled()
            if (snippetExpression.type == CwtDataTypes.Constant) return@f
            val matchGroup = matchResult.groups.get(i++) ?: return false
            val matchValue = matchGroup.value
            if (matchValue.isEmpty() && snippetExpression.type == CwtDataTypes.Definition) return false // skip anonymous definitions
            val matchContext = ParadoxExpressionMatchContext(element, ParadoxExpression.resolve(matchValue), configGroup, options)
            val matched = matchScriptExpression(matchContext, snippetExpression, null).get(options)
            if (!matched) return false
        }
        return true
    }

    fun getMatchedScriptUnionCandidate(element: PsiElement, expression: ParadoxExpression, unionName: String, configGroup: CwtConfigGroup, options: ParadoxMatchOptions? = null): CwtValueConfig? {
        val unionConfig = configGroup.unions[unionName] ?: return null
        // NOTE 3.0.1 recursion guard is not directly required here
        return ProcessorScope.findFrom {
            unionConfig.expandUnionCandidates { valueConfig ->
                ProgressManager.checkCanceled()
                val matchContext = ParadoxExpressionMatchContext(element, expression, configGroup, options)
                val matched = matchScriptExpression(matchContext, valueConfig.configExpression, valueConfig).get(options)
                if (matched) process(valueConfig) else true
            }
        }
    }

    fun getMatchedCsvUnionCandidate(element: PsiElement, expression: ParadoxExpression, unionName: String, configGroup: CwtConfigGroup): CwtValueConfig? {
        val unionConfig = configGroup.unions[unionName] ?: return null
        // NOTE 3.0.1 recursion guard is not directly required here
        return ProcessorScope.findFrom {
            unionConfig.expandUnionCandidates { valueConfig ->
                ProgressManager.checkCanceled()
                val matchContext = ParadoxExpressionMatchContext(element, expression, configGroup)
                if (matchCsvExpression(matchContext, valueConfig.configExpression).get()) process(valueConfig)
                else true
            }
        }
    }

    fun getMatchedAliasKey(element: PsiElement, expression: ParadoxExpression, aliasName: String, configGroup: CwtConfigGroup, options: ParadoxMatchOptions? = null): String? {
        // NOTE 3.0.3 fast return if the alias key can be matched constantly (case-insensitive), otherwise, try further match
        val constKey = configGroup.aliasModel.name2ConstKeys[aliasName]?.get(expression.value)
        if (constKey != null) return constKey

        // NOTE 3.0.3 should also include non-const keys if the expression is parameterized
        val keys = when {
            expression.isParameterized() -> configGroup.aliasGroups[aliasName]?.keys
            else -> configGroup.aliasKeysGroupNoConst[aliasName]
        }
        if (keys.isNullOrEmpty()) return null

        ProgressManager.checkCanceled() // check cancellation
        val matchContext = ParadoxExpressionMatchContext(element, expression, configGroup, options)
        return keys.find { key ->
            matchScriptExpression(matchContext, CwtDataExpression.resolve(key), null).get(options)
        }
    }
}
