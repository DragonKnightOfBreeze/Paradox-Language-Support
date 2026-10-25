package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.manipulation.CwtConfigExpansionService
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.manipulation.ParadoxConfigExpansionService
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.model.expressions.ParadoxExpression

abstract class ParadoxExpandableCsvExpressionSupport : ParadoxCsvExpressionSupport {
    /**
     * @see CwtDataTypes.UnionValue
     */
    class ForUnionValue : ParadoxExpandableCsvExpressionSupport() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.UnionValue

        // NOTE 3.0.1 recursion guard is required here for various operations

        override fun annotate(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig, holder: AnnotationHolder): Boolean {
            val configGroup = config.configGroup
            val configExpression = config.configExpression
            val unionName = configExpression.metadata.value ?: return false
            val expression = ParadoxExpression.resolve(text, false) // 3.0.3 should be resolved from `text`, rather than `element`
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first actually matched config atm, event if the result from this config is null or empty
            val guardName = "csvExpression.annotate.union"
            val expanded = ParadoxConfigExpansionService.expandAndMatchUnion(element, expression, unionName, configGroup, guardName = guardName)
            val result = expanded.filter { _, mr -> mr.get() }.any { r, _ ->
                ParadoxExpressionService.annotateScriptExpression(element, text, rangeInExpression, r.config, holder)
            }
            return result
        }

        override fun resolve(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): PsiElement? {
            val configGroup = config.configGroup
            val configExpression = config.configExpression
            val unionName = configExpression.metadata.value ?: return null
            val expression = ParadoxExpression.resolve(text, false) // 3.0.3 should be resolved from `text`, rather than `element`
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first actually matched config atm, event if the result from this config is null or empty
            val guardName = "csvExpression.resolve.union"
            val expanded = ParadoxConfigExpansionService.expandAndMatchUnion(element, expression, unionName, configGroup, guardName = guardName)
            val result = expanded.filter { _, mr -> mr.get() }.firstNotNullOrOrNull { r, _ ->
                ParadoxExpressionService.resolveCsvExpression(element, text, rangeInExpression, r.config)
            }
            return result
        }

        override fun resolveAll(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): List<PsiElement> {
            val configGroup = config.configGroup
            val configExpression = config.configExpression
            val unionName = configExpression.metadata.value ?: return emptyList()
            val expression = ParadoxExpression.resolve(text, false) // 3.0.3 should be resolved from `text`, rather than `element`
            // NOTE 3.0.1 recursion guard is required here
            val guardName = "csvExpression.resolveAll.union"
            val expanded = ParadoxConfigExpansionService.expandAndMatchUnion(element, expression, unionName, configGroup, guardName = guardName)
            val result = expanded.filter { _, mr -> mr.get() }.firstNotNullOrOrNull { r, _ ->
                ParadoxExpressionService.resolveAllCsvExpression(element, text, rangeInExpression, r.config).orNull()
            }
            return result.orEmpty()
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            val configGroup = context.configGroup
            val configExpression = context.config?.configExpression ?: return
            ProgressManager.checkCanceled()
            // NOTE 3.0.3 recursion guard is required here
            val guardName = "csvExpression.complete"
            val expanded = CwtConfigExpansionService.expandUnion(configExpression, configGroup, guardName = guardName)
            expanded.forEach { r ->
                val context = context.copy(config = r.config, configs = r.configs)
                ParadoxExpressionCompletionManager.completeCsvExpression(context, result)
            }
        }
    }
}
