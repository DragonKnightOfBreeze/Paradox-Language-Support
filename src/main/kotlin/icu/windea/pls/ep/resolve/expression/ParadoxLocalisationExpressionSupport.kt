package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.base.annotations.WithGameTypeEP
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement

/**
 * 提供对本地化表达式的支持。
 *
 * 用于实现代码高亮、引用解析、代码补全等语言功能。
 *
 *
 * @see ParadoxExpressionElement
 * @see ParadoxLocalisationExpressionElement
 */
@WithGameTypeEP
interface ParadoxLocalisationExpressionSupport {
    fun supports(element: ParadoxExpressionElement): Boolean

    fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, holder: AnnotationHolder) {

    }

    fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String): PsiElement? {
        return null
    }

    fun resolveAll(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String): List<PsiElement> {
        return resolve(element, rangeInElement, text).to.singletonListOrEmpty()
    }

    fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String): List<PsiReference> {
        return emptyList()
    }

    fun complete(context: ProcessingContext, result: CompletionResultSet) {

    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxLocalisationExpressionSupport>("icu.windea.pls.localisationExpressionSupport")
    }
}
