package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.type.ParadoxExpressionRole

abstract class ParadoxSnippetScriptExpressionSupport: ParadoxScriptExpressionSupport {
    /**
     * @see CwtDataTypes.DefinitionSnippet
     */
    class ForDefinitionSnippet: ParadoxSnippetScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.DefinitionSnippet
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            return super.annotate(element, text, rangeInExpression, config, holder) // TODO 3.0.4
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            return super.resolve(element, text, rangeInExpression, config, role) // TODO 3.0.4
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            super.complete(context, result) // TODO 3.0.4
        }
    }

    /**
     * @see CwtDataTypes.LocalisationSnippet
     */
    class ForLocalisationSnippet: ParadoxSnippetScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.LocalisationSnippet
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            return super.annotate(element, text, rangeInExpression, config, holder) // TODO 3.0.4
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            return super.resolve(element, text, rangeInExpression, config, role) // TODO 3.0.4
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            super.complete(context, result) // TODO 3.0.4
        }
    }
}
