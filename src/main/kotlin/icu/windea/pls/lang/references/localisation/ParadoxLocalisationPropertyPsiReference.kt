package icu.windea.pls.lang.references.localisation

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.parameter.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.ParadoxLocalisationCategory.*

/**
 * @see icu.windea.pls.lang.codeInsight.completion.localisation.ParadoxLocalisationParameterCompletionProvider
 */
class ParadoxLocalisationPropertyPsiReference(
    element: ParadoxLocalisationParameter,
    rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationParameter>(element, rangeInElement) {
    val project by lazy { element.project }

    override fun handleElementRename(newElementName: String): PsiElement {
        //TODO 重命名关联的definition
        return element.setName(newElementName)
    }

    //缓存解析结果以优化性能

    private object Resolver : ResolveCache.AbstractResolver<ParadoxLocalisationPropertyPsiReference, PsiElement> {
        override fun resolve(ref: ParadoxLocalisationPropertyPsiReference, incompleteCode: Boolean) = ref.doResolve()
    }

    private object MultiResolver : ResolveCache.PolyVariantResolver<ParadoxLocalisationPropertyPsiReference> {
        override fun resolve(ref: ParadoxLocalisationPropertyPsiReference, incompleteCode: Boolean) = ref.doMultiResolve()
    }

    private object LocResolver : ResolveCache.AbstractResolver<ParadoxLocalisationPropertyPsiReference, PsiElement> {
        override fun resolve(ref: ParadoxLocalisationPropertyPsiReference, incompleteCode: Boolean) = ref.doResolve(onlyLocalisation = true)
    }

    override fun resolve(): PsiElement? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
    }

    fun resolveLocalisation(): ParadoxLocalisationProperty? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, LocResolver, false, false).castOrNull()
    }

    private fun doResolve(onlyLocalisation: Boolean = false): PsiElement? {
        val element = element
        val file = element.containingFile as? ParadoxLocalisationFile ?: return null
        val category = ParadoxLocalisationCategory.resolve(file) ?: return null
        val locale = selectLocale(file)
        val name = element.name

        //尝试解析成localisation或者synced_localisation
        val selector = selector(project, file).localisation().contextSensitive().preferLocale(locale)
        val resolved = when (category) {
            Normal -> ParadoxLocalisationSearch.search(name, selector).find()
            Synced -> ParadoxSyncedLocalisationSearch.search(name, selector).find()
        }
        if (resolved != null) return resolved
        if (onlyLocalisation) return null

        //尝试解析成parameter
        val resolvedParameter = ParadoxLocalisationParameterSupport.resolveParameter(element)
        if (resolvedParameter != null) return resolvedParameter

        return null
    }

    private fun doMultiResolve(onlyLocalisation: Boolean = false): Array<out ResolveResult> {
        val element = element
        val file = element.containingFile as? ParadoxLocalisationFile ?: return emptyArray()
        val category = ParadoxLocalisationCategory.resolve(file) ?: return emptyArray()
        val locale = selectLocale(file)
        val name = element.name

        //尝试解析成localisation或者synced_localisation
        val selector = selector(project, file).localisation().contextSensitive().preferLocale(locale)
        val resolved = when (category) {
            Normal -> ParadoxLocalisationSearch.search(name, selector).findAll() //查找所有语言区域的
            Synced -> ParadoxSyncedLocalisationSearch.search(name, selector).findAll() //查找所有语言区域的
        }
        if (resolved.isNotEmpty()) return resolved.mapToArray { PsiElementResolveResult(it) }
        if (onlyLocalisation) return ResolveResult.EMPTY_ARRAY

        //尝试解析成localisation_parameter
        val resolvedParameter = ParadoxLocalisationParameterSupport.resolveParameter(element)
        if (resolvedParameter != null) return resolvedParameter.let { arrayOf(PsiElementResolveResult(it)) }

        return ResolveResult.EMPTY_ARRAY
    }
}
