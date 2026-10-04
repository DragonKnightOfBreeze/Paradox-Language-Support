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

/**
 * @see CwtDataTypeSets.Snippet
 */
abstract class ParadoxSnippetCsvExpressionSupport : ParadoxCsvExpressionSupport {
    /**
     * @see CwtDataTypes.DefinitionSnippet
     */
    class ForDefinitionSnippet : ParadoxSnippetCsvExpressionSupport() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.DefinitionSnippet

        override fun annotate(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig, holder: AnnotationHolder): Boolean {
            val attributesKey = ParadoxSemanticHighlighterColors.definitionReferenceSnippet(element.language)
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): PsiElement? {
            return ParadoxExpressionSupportFactory.resolveDefinitionSnippet(element, text, config)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            ParadoxExpressionCompletionManager.completeDefinitionSnippet(context, result)
        }
    }
}
