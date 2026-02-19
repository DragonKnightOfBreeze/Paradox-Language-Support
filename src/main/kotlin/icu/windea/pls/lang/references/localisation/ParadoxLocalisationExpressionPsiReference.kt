package icu.windea.pls.lang.references.localisation

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.createResults
import icu.windea.pls.core.psi.PsiReferencesAware
import icu.windea.pls.lang.psi.ParadoxPsiManager
import icu.windea.pls.lang.resolve.ParadoxLocalisationExpressionService
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager.getExpressionText
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement

class ParadoxLocalisationExpressionPsiReference(
    element: ParadoxLocalisationExpressionElement,
    rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationExpressionElement>(element, rangeInElement), PsiReferencesAware {
    private val project get() = element.project

    override fun handleElementRename(newElementName: String): PsiElement {
        return ParadoxPsiManager.handleExpressionElementRename(element, rangeInElement, newElementName, resolve())
    }

    override fun getReferences(): Array<out PsiReference>? {
        val expressionText = getExpressionText(element, rangeInElement)
        val result = ParadoxLocalisationExpressionService.getReferences(element, rangeInElement, expressionText)
        return result.orNull()
    }

    // 缓存解析结果以优化性能

    private object Resolver : ResolveCache.AbstractResolver<ParadoxLocalisationExpressionPsiReference, PsiElement> {
        override fun resolve(ref: ParadoxLocalisationExpressionPsiReference, incompleteCode: Boolean) = ref.doResolve()
    }

    private object MultiResolver : ResolveCache.PolyVariantResolver<ParadoxLocalisationExpressionPsiReference> {
        override fun resolve(ref: ParadoxLocalisationExpressionPsiReference, incompleteCode: Boolean) = ref.doMultiResolve()
    }

    override fun resolve(): PsiElement? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
    }

    private fun doResolve(): PsiElement? {
        // 根据对应的 expression 进行解析
        val resolved = ParadoxExpressionManager.resolveLocalisationExpression(element, rangeInElement)
        return resolved
    }

    private fun doMultiResolve(): Array<out ResolveResult> {
        // 根据对应的 expression 进行解析
        val resolved = ParadoxExpressionManager.multiResolveLocalisationExpression(element, rangeInElement)
        return resolved.createResults()
    }
}
