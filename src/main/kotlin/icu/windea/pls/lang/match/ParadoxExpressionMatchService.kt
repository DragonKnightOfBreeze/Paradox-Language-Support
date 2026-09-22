package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.manipulation.CwtConfigExpansionService
import icu.windea.pls.config.util.CwtConfigExpressionManager
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.util.ProcessorFactory
import icu.windea.pls.ep.match.expression.ParadoxCsvExpressionMatcher
import icu.windea.pls.ep.match.expression.ParadoxScriptExpressionMatchOptimizer
import icu.windea.pls.ep.match.expression.ParadoxScriptExpressionMatcher
import icu.windea.pls.lang.ParadoxThreadContext
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.script.ParadoxScriptLanguage

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
            CwtDataTypes.Value, CwtDataTypes.DynamicValue -> {
                val type = configExpression.metadata.value ?: return false
                val dynamicValueConfig = configGroup.dynamicValueTypes[type] ?: return false
                dynamicValueConfig.values.contains(expression.value)
            }
            // NOTE 3.0.3 for expandable data types, only include union values here
            CwtDataTypes.UnionValue -> {
                // NOTE 3.0.3 recursion guard is required here
                val processor = ProcessorFactory.any<Unit>()
                CwtConfigExpansionService.expandUnion(configExpression, configGroup, "exprssion.matchesConstant") { e, _ ->
                    if (matchesConstant(expression, e, configGroup)) processor.process(Unit) else true
                }
            }
            else -> false
        }
    }

    fun matchesTemplate(element: PsiElement, expression: ParadoxExpression, templateExpression: CwtTemplateExpression, configGroup: CwtConfigGroup, options: ParadoxMatchOptions? = null): Boolean {
        val language = element.language
        if (language != ParadoxScriptLanguage) return false
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
}
