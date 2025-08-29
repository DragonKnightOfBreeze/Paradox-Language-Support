package icu.windea.pls.lang.references.localisation

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.core.collections.mapToArray
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.psi.PsiReferencesAware
import icu.windea.pls.ep.expression.ParadoxLocalisationExpressionSupport
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager.getExpressionText
import icu.windea.pls.lang.util.ParadoxPsiManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement

class ParadoxLocalisationExpressionPsiReference(
    element: ParadoxLocalisationExpressionElement,
    rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationExpressionElement>(element, rangeInElement), PsiReferencesAware {
    val project by lazy { element.project }

    override fun handleElementRename(newElementName: String): PsiElement {
        return ParadoxPsiManager.handleElementRename(element, rangeInElement, newElementName)
    }

    override fun getReferences(): Array<out PsiReference>? {
        ProgressManager.checkCanceled()
        val expressionText = getExpressionText(element, rangeInElement)

        val result = ParadoxLocalisationExpressionSupport.getReferences(element, rangeInElement, expressionText)
        return result.orNull()
    }

    //缓存解析结果以优化性能

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
        //根据对应的expression进行解析
        return ParadoxExpressionManager.resolveLocalisationExpression(element, rangeInElement)
    }

    private fun doMultiResolve(): Array<out ResolveResult> {
        //根据对应的expression进行解析
        return ParadoxExpressionManager.multiResolveLocalisationExpression(element, rangeInElement)
            .mapToArray { PsiElementResolveResult(it) }
    }
}
