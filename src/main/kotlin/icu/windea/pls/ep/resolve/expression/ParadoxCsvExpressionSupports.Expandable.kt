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
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.util.ProcessorFactory
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
            val unionName = config.configExpression.metadata.value ?: return false
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first actually matched config atm, event if the result from this config is null or empty
            val processor = ProcessorFactory.find<CwtValueConfig>()
            runWithRecursionGuard("csvExpression.annotate.union", unionName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedUnion(element, expression, unionName, configGroup) {
                    processor.process(it)
                }
            }
            val unionValueConfig = processor.result ?: return false
            return ParadoxExpressionService.annotateCsvExpression(element, text, rangeInExpression, unionValueConfig, holder)
        }

        override fun resolve(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): PsiElement? {
            val configGroup = config.configGroup
            val unionName = config.configExpression.metadata.value ?: return null
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first actually matched config atm, event if the result from this config is null or empty
            val processor = ProcessorFactory.find<PsiElement>()
            runWithRecursionGuard("csvExpression.resolve.union", unionName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedUnion(element, expression, unionName, configGroup) p@{ unionValueConfig ->
                    val r = ParadoxExpressionService.resolveCsvExpression(element, text, rangeInExpression, unionValueConfig)
                    if (r == null) return@p true
                    processor.process(r)
                }
            }
            return processor.result
        }

        override fun resolveAll(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): List<PsiElement> {
            val configGroup = config.configGroup
            val unionName = config.configExpression.metadata.value ?: return emptyList()
            // NOTE 3.0.1 recursion guard is required here
            val processor = ProcessorFactory.find<List<PsiElement>>()
            runWithRecursionGuard("csvExpression.resolveAll.union", unionName) {
                val expression = ParadoxExpression.resolve(element)
                ParadoxConfigExpansionService.expandMatchedUnion(element, expression, unionName, configGroup) p@{ unionValueConfig ->
                    val r = ParadoxExpressionService.resolveAllCsvExpression(element, text, rangeInExpression, unionValueConfig).orNull()
                    if (r == null) return@p true
                    processor.process(r)
                }
            }
            return processor.result.orEmpty()
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            val configGroup = context.configGroup
            val configExpression = context.config?.configExpression ?: return
            ProgressManager.checkCanceled()
            // NOTE 3.0.3 recursion guard is required here
            CwtConfigExpansionService.expandUnion(configExpression, configGroup, "csvExpression.complete") { _, unionValueConfig ->
                val context = context.copy(config = unionValueConfig, configs = setOf(unionValueConfig))
                ParadoxExpressionCompletionManager.completeCsvExpression(context, result)
                true
            }
        }
    }
}
