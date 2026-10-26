package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.manipulation.CwtConfigExpansionService
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.manipulation.ParadoxConfigExpansionService
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.model.type.ParadoxExpressionRole

abstract class ParadoxExpandableScriptExpressionSupport : ParadoxScriptExpressionSupport {
    /**
     * @see CwtDataTypes.UnionValue
     */
    class ForUnionValue : ParadoxExpandableScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.UnionValue

        // NOTE 3.0.1 recursion guard is required here for various operations
        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return false
            val unionName = configExpression.metadata.value ?: return false
            val expression = ParadoxExpression.resolve(text, false) // 3.0.3 should be resolved from `text`, rather than `element`
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first actually matched config atm, event if the result from this config is null or empty
            val guardName = "scriptExpression.annotate.union"
            val expanded = ParadoxConfigExpansionService.expandAndMatchUnion(element, expression, unionName, configGroup, guardName = guardName)
            return expanded.filter { _, mr -> mr.get() }.any { r, _ ->
                ParadoxExpressionService.annotateScriptExpression(element, text, rangeInExpression, r.config, holder)
            }
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return null
            val unionName = configExpression.metadata.value ?: return null
            val expression = ParadoxExpression.resolve(text, false) // 3.0.3 should be resolved from `text`, rather than `element`
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first actually matched config atm, event if the result from this config is null or empty
            val guardName = "scriptExpression.resolve.union"
            val expanded = ParadoxConfigExpansionService.expandAndMatchUnion(element, expression, unionName, configGroup, guardName = guardName)
            val matched = expanded.firstNotNullOrOrNull { m, mr -> if (mr.get()) m else null } ?: return null
            return ParadoxExpressionService.resolveScriptExpression(element, text, rangeInExpression, matched.config, role)
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return emptyList()
            val unionName = configExpression.metadata.value ?: return emptyList()
            val expression = ParadoxExpression.resolve(text, false) // 3.0.3 should be resolved from `text`, rather than `element`
            // NOTE 3.0.1 recursion guard is required here
            val guardName = "scriptExpression.resolveAll.union"
            val expanded = ParadoxConfigExpansionService.expandAndMatchUnion(element, expression, unionName, configGroup, guardName = guardName)
            val matched = expanded.firstNotNullOrOrNull { m, mr -> if (mr.get()) m else null } ?: return emptyList()
            return ParadoxExpressionService.resolveAllScriptExpression(element, text, rangeInExpression, matched.config, role)
        }

        override fun getReferences(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiReference> {
            // #374 `union[x]` 同样需要兼容这里，目前来说，这是和 `alias_keys_field[x]` 不同的地方（例如，对于 `union[test_union] = { value[test_flag] }`，其中的 `value[test_flag]` 可以匹配多个节点）
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return emptyList()
            val unionName = configExpression.metadata.value ?: return emptyList()
            val expression = ParadoxExpression.resolve(text, false) // 3.0.3 should be resolved from `text`, rather than `element`
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first actually matched config atm, event if the result from this config is null or empty
            val guardName = "scriptExpression.getReferences.union"
            val expanded = ParadoxConfigExpansionService.expandAndMatchUnion(element, expression, unionName, configGroup, guardName = guardName)
            val matched = expanded.firstNotNullOrOrNull { m, mr -> if (mr.get()) m else null } ?: return emptyList()
            return ParadoxExpressionService.getScriptExpressionReferences(element, text, rangeInExpression, matched.config, role)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            val configGroup = context.configGroup
            val configExpression = context.config?.configExpression ?: return
            ProgressManager.checkCanceled()
            // NOTE 3.0.3 recursion guard is required here
            val guardName = "scriptExpression.complete"
            val expanded = CwtConfigExpansionService.expandUnion(configExpression, configGroup, guardName = guardName)
            expanded.forEach { r ->
                val context = context.copy(config = r.config, configs = setOf(r.config))
                ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
            }
        }
    }

    /**
     * @see CwtDataTypes.AliasKeysField
     * @see CwtDataTypes.AliasName
     */
    class ForAliasName : ParadoxExpandableScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.AliasKeysField || dataType == CwtDataTypes.AliasName
        }

        // NOTE 3.0.1 recursion guard is required here for various operations

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return false
            val aliasName = configExpression.metadata.value ?: return false
            val expression = ParadoxExpression.resolve(text, false) // 3.0.3 should be resolved from `text`, rather than `element`
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first actually matched config atm, event if the result from this config is null or empty
            val guardName = "scriptExpression.annotate.alias"
            val expanded = ParadoxConfigExpansionService.expandAndMatchAlias(element, expression, aliasName, configGroup, guardName = guardName)
            return expanded.filter { _, mr -> mr.get() }.any { r, _ ->
                ParadoxExpressionService.annotateScriptExpression(element, text, rangeInExpression, r.config, holder)
            }
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return null
            val aliasName = configExpression.metadata.value ?: return null
            val expression = ParadoxExpression.resolve(text, false) // 3.0.3 should be resolved from `text`, rather than `element`
            // NOTE 3.0.1 recursion guard is required here
            val guardName = "scriptExpression.resolve.alias"
            val expanded = ParadoxConfigExpansionService.expandAndMatchAlias(element, expression, aliasName, configGroup, guardName = guardName)
            val matched = expanded.firstNotNullOrOrNull { m, mr -> if (mr.get()) m else null } ?: return null
            return ParadoxExpressionService.resolveScriptExpression(element, text, rangeInExpression, matched.config, role)
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            val configGroup = config.configGroup
            val configExpression = config.configExpression ?: return emptyList()
            val aliasName = configExpression.metadata.value ?: return emptyList()
            val expression = ParadoxExpression.resolve(text, false) // 3.0.3 should be resolved from `text`, rather than `element`
            // NOTE 3.0.1 recursion guard is required here
            val guardName = "scriptExpression.resolveAll.alias"
            val expanded = ParadoxConfigExpansionService.expandAndMatchAlias(element, expression, aliasName, configGroup, guardName = guardName)
            val matched = expanded.firstNotNullOrOrNull { m, mr -> if (mr.get()) m else null } ?: return emptyList()
            return ParadoxExpressionService.resolveAllScriptExpression(element, text, rangeInExpression, matched.config, role)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            val configGroup = context.configGroup
            val configExpression = context.config?.configExpression ?: return
            ProgressManager.checkCanceled()
            // NOTE 3.0.3 recursion guard is required here
            val guardName = "scriptExpression.complete"
            val expanded = CwtConfigExpansionService.expandAlias(configExpression, configGroup, guardName = guardName)
            expanded.forEach { r ->
                val context = context.copy(config = r.config, configs = r.configs)
                ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
            }
        }
    }
}
