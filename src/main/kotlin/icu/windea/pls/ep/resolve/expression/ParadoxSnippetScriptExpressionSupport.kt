package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.util.ParadoxExpressionSupportFactory
import icu.windea.pls.model.type.ParadoxExpressionRole

/**
 * 提供对脚本表达式中的片段（[CwtDataTypes.DefinitionSnippet] [CwtDataTypes.LocalisationSnippet]）的支持。
 *
 * 说明：
 * - 这并非完整匹配，因此引用解析得到的是一个 lightElement（相关项），而非直接解析为对应的定义或本地化。
 * - 采用宽松策略，在语义匹配阶段不要求存在所有对应的引用，完整性问题由代码检查负责报告。
 *
 * @see ParadoxSnippetCsvExpressionSupport
 */
abstract class ParadoxSnippetScriptExpressionSupport : ParadoxScriptExpressionSupport {
    /**
     * @see CwtDataTypes.DefinitionSnippet
     */
    class ForDefinitionSnippet : ParadoxSnippetScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.DefinitionSnippet
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            ParadoxExpressionSupportFactory.annotateExpressionAsHighlightedReference(element, rangeInExpression, holder)
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            return ParadoxExpressionSupportFactory.resolveDefinitionSnippet(element, text, config)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            ParadoxExpressionCompletionManager.completeDefinitionSnippet(context, result)
        }
    }

    /**
     * @see CwtDataTypes.LocalisationSnippet
     */
    class ForLocalisationSnippet : ParadoxSnippetScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.LocalisationSnippet
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            ParadoxExpressionSupportFactory.annotateExpressionAsHighlightedReference(element, rangeInExpression, holder)
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            return ParadoxExpressionSupportFactory.resolveLocalisationSnippet(element, text, config)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            ParadoxExpressionCompletionManager.completeLocalisationSnippet(context, result)
        }
    }
}
