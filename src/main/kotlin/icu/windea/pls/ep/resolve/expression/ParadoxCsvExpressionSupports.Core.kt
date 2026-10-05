package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.highlighting.ParadoxSemanticHighlighterColors
import icu.windea.pls.lang.resolve.util.ParadoxExpressionSupportFactory

abstract class ParadoxCoreCsvExpressionSupport : ParadoxCsvExpressionSupport {
    /**
     * @see CwtDataTypes.Definition
     */
    class ForDefinition : ParadoxCoreCsvExpressionSupport() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Definition

        override fun annotate(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig, holder: AnnotationHolder): Boolean {
            val attributesKey = ParadoxSemanticHighlighterColors.definitionReference(element.language)
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): PsiElement? {
            return ParadoxExpressionSupportFactory.resolveDefinition(element, text, config)
        }

        override fun resolveAll(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): List<PsiElement> {
            return ParadoxExpressionSupportFactory.resolveAllDefinition(element, text, config)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            ParadoxExpressionCompletionManager.completeDefinition(context, result)
        }
    }

    /**
     * @see CwtDataTypes.EnumValue
     */
    class ForEnumValue : ParadoxCoreCsvExpressionSupport() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.EnumValue

        override fun annotate(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig, holder: AnnotationHolder): Boolean {
            val configGroup = config.configGroup
            val configExpression = config.configExpression
            val enumName = configExpression.metadata.value ?: return false
            val attributesKey = when {
                configGroup.complexEnums[enumName] != null -> ParadoxSemanticHighlighterColors.complexEnumValue(element.language)
                else -> ParadoxSemanticHighlighterColors.enumValue(element.language)
            }
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): PsiElement? {
            return ParadoxExpressionSupportFactory.resolveEnumValue(element, text, config)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            ParadoxExpressionCompletionManager.completeEnumValue(context, result)
        }
    }

    /**
     * @see CwtDataTypeSets.DynamicValue
     */
    class ForDynamicValue : ParadoxCoreCsvExpressionSupport() {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType in CwtDataTypeSets.DynamicValue
        }

        override fun annotate(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig, holder: AnnotationHolder): Boolean {
            val attributesKey = ParadoxSemanticHighlighterColors.dynamicValue(element.language)
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): PsiElement? {
            return ParadoxExpressionSupportFactory.resolveDynamicValue(element, text, config)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            ParadoxExpressionCompletionManager.completeDynamicValue(context, result)
        }
    }
}
