package icu.windea.pls.localisation.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.icon.*
import icu.windea.pls.localisation.psi.*

/**
 * 本地化图标的PSI引用。
 *
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationIconCompletionProvider
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
        override fun resolve(ref: ParadoxLocalisationIconPsiReference, incompleteCode: Boolean): PsiElement? {
            return ref.doResolve()
        }
    }

    private object MultiResolver : ResolveCache.PolyVariantResolver<ParadoxLocalisationIconPsiReference> {
        override fun resolve(ref: ParadoxLocalisationIconPsiReference, incompleteCode: Boolean): Array<out ResolveResult> {
            return ref.doMultiResolve()
        }
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
