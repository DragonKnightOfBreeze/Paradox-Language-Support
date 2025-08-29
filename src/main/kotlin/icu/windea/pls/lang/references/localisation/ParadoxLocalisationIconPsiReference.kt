package icu.windea.pls.lang.references.localisation

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.core.collections.mapToArray
import icu.windea.pls.ep.icon.ParadoxLocalisationIconSupport
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

/**
 * 本地化图标的PSI引用。
 *
 * @see icu.windea.pls.lang.codeInsight.completion.localisation.ParadoxLocalisationIconCompletionProvider
 */
class ParadoxLocalisationIconPsiReference(
    element: ParadoxLocalisationIcon,
    rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationIcon>(element, rangeInElement) {
    val project by lazy { element.project }

    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setName(newElementName)
    }

    //缓存解析结果以优化性能

    private object Resolver : ResolveCache.AbstractResolver<ParadoxLocalisationIconPsiReference, PsiElement> {
        override fun resolve(ref: ParadoxLocalisationIconPsiReference, incompleteCode: Boolean) = ref.doResolve()
    }

    private object MultiResolver : ResolveCache.PolyVariantResolver<ParadoxLocalisationIconPsiReference> {
        override fun resolve(ref: ParadoxLocalisationIconPsiReference, incompleteCode: Boolean) = ref.doMultiResolve()
    }

    override fun resolve(): PsiElement? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
    }

    private fun doResolve(): PsiElement? {
        val element = element
        val name = element.name
        if (name.isNullOrEmpty()) return null
        val resolved = ParadoxLocalisationIconSupport.resolve(name, element, project)
        return resolved
    }

    private fun doMultiResolve(): Array<out ResolveResult> {
        val element = element
        val name = element.name
        if (name.isNullOrEmpty()) return ResolveResult.EMPTY_ARRAY
        val resolved = ParadoxLocalisationIconSupport.resolveAll(name, element, project)
        if (resolved.isEmpty()) return ResolveResult.EMPTY_ARRAY
        return resolved.mapToArray { PsiElementResolveResult(it) }
    }
}
