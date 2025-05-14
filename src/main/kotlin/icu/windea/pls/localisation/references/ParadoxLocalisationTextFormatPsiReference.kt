package icu.windea.pls.localisation.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*

/**
 * 本地化格式的PSI引用。
 *
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationTextFormatCompletionProvider
 */
@WithGameType(ParadoxGameType.Ck3, ParadoxGameType.Vic3)
class ParadoxLocalisationTextFormatPsiReference(
    element: ParadoxLocalisationTextFormat,
    rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationTextFormat>(element, rangeInElement) {
    val project by lazy { element.project }

    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setName(newElementName)
    }

    //缓存解析结果以优化性能

    private object Resolver : ResolveCache.AbstractResolver<ParadoxLocalisationTextFormatPsiReference, PsiElement> {
        override fun resolve(ref: ParadoxLocalisationTextFormatPsiReference, incompleteCode: Boolean): PsiElement? {
            return ref.doResolve()
        }
    }

    private object MultiResolver : ResolveCache.PolyVariantResolver<ParadoxLocalisationTextFormatPsiReference> {
        override fun resolve(ref: ParadoxLocalisationTextFormatPsiReference, incompleteCode: Boolean): Array<out ResolveResult> {
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
        val definitionName = element.name?.orNull() ?: return null
        val definitionType = ParadoxDefinitionTypes.TextFormat
        val definitionSelector = selector(project, element).definition().contextSensitive()
        val resolved = ParadoxDefinitionSearch.search(definitionName, definitionType, definitionSelector).find()
        return resolved
    }

    private fun doMultiResolve(): Array<out ResolveResult> {
        val element = element
        val definitionName = element.name?.orNull() ?: return ResolveResult.EMPTY_ARRAY
        val definitionType = ParadoxDefinitionTypes.TextFormat
        val definitionSelector = selector(project, element).definition().contextSensitive()
        val resolved = ParadoxDefinitionSearch.search(definitionName, definitionType, definitionSelector).findAll()
        if (resolved.isEmpty()) return ResolveResult.EMPTY_ARRAY
        return resolved.mapToArray { PsiElementResolveResult(it) }
    }
}
