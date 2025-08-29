package icu.windea.pls.lang.references.localisation

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.core.annotations.WithGameType
import icu.windea.pls.core.collections.mapToArray
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

/**
 * 本地化文本图标的PSI引用。
 *
 * @see icu.windea.pls.lang.codeInsight.completion.localisation.ParadoxLocalisationTextIconCompletionProvider
 */
@WithGameType(ParadoxGameType.Ck3, ParadoxGameType.Vic3)
class ParadoxLocalisationTextIconPsiReference(
    element: ParadoxLocalisationTextIcon,
    rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationTextIcon>(element, rangeInElement) {
    val project by lazy { element.project }

    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setName(newElementName)
    }

    //缓存解析结果以优化性能

    private object Resolver : ResolveCache.AbstractResolver<ParadoxLocalisationTextIconPsiReference, PsiElement> {
        override fun resolve(ref: ParadoxLocalisationTextIconPsiReference, incompleteCode: Boolean) = ref.doResolve()
    }

    private object MultiResolver : ResolveCache.PolyVariantResolver<ParadoxLocalisationTextIconPsiReference> {
        override fun resolve(ref: ParadoxLocalisationTextIconPsiReference, incompleteCode: Boolean) = ref.doMultiResolve()
    }

    override fun resolve(): PsiElement? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
    }

    private fun doResolve(): PsiElement? {
        val element = element
        val definitionName = element.name?.orNull() ?: return null
        val definitionType = ParadoxDefinitionTypes.TextIcon
        val definitionSelector = selector(project, element).definition().contextSensitive()
        val resolved = ParadoxDefinitionSearch.search(definitionName, definitionType, definitionSelector).find()
        return resolved
    }

    private fun doMultiResolve(): Array<out ResolveResult> {
        val element = element
        val definitionName = element.name?.orNull() ?: return ResolveResult.EMPTY_ARRAY
        val definitionType = ParadoxDefinitionTypes.TextIcon
        val definitionSelector = selector(project, element).definition().contextSensitive()
        val resolved = ParadoxDefinitionSearch.search(definitionName, definitionType, definitionSelector).findAll()
        if (resolved.isEmpty()) return ResolveResult.EMPTY_ARRAY
        return resolved.mapToArray { PsiElementResolveResult(it) }
    }
}
