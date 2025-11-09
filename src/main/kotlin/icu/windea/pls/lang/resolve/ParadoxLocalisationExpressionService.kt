package icu.windea.pls.lang.resolve

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.ep.resolve.expression.ParadoxLocalisationExpressionSupport
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.codeInsight.completion.contextElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.selectGameType

object ParadoxLocalisationExpressionService {
    // 目前来看，这里暂时不需要尝试避免 SOE

    /**
     * @see ParadoxLocalisationExpressionSupport.annotate
     */
    fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder) {
        val gameType = selectGameType(element)
        ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
            if (!ep.supports(element)) return@f
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            ep.annotate(element, rangeInElement, expressionText, holder)
        }
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.resolve
     */
    fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): PsiElement? {
        val gameType = selectGameType(element)
        return ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!ep.supports(element)) return@f null
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            val r = ep.resolve(element, rangeInElement, expressionText)
            r
        }
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.multiResolve
     */
    fun multiResolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): Collection<PsiElement> {
        val gameType = selectGameType(element)
        return ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!ep.supports(element)) return@f null
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            val r = ep.multiResolve(element, rangeInElement, expressionText).orNull()
            r
        }.orEmpty()
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.getReferences
     */
    fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): Array<out PsiReference>? {
        val gameType = selectGameType(element)
        ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
            if (!ep.supports(element)) return@f
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            val r = ep.getReferences(element, rangeInElement, expressionText).orNull()
            if (r != null) return r
        }
        return null
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.complete
     */
    fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val gameType by lazy { selectGameType(element) }
        ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
            if (!ep.supports(element)) return@f
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            ep.complete(context, result)
        }
    }
}
