package icu.windea.pls.lang.references.localisation

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.mapToArray
import icu.windea.pls.ep.parameter.ParadoxLocalisationParameterSupport
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxSyncedLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.ParadoxLocalisationType.Normal
import icu.windea.pls.model.ParadoxLocalisationType.Synced

/**
 * @see icu.windea.pls.lang.codeInsight.completion.localisation.ParadoxLocalisationParameterCompletionProvider
 */
class ParadoxLocalisationParameterPsiReference(
    element: ParadoxLocalisationParameter,
    rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationParameter>(element, rangeInElement) {
    val project by lazy { element.project }

    override fun handleElementRename(newElementName: String): PsiElement {
        //TODO 重命名关联的definition
        return element.setName(newElementName)
    }

    //缓存解析结果以优化性能

    private object Resolver : ResolveCache.AbstractResolver<ParadoxLocalisationParameterPsiReference, PsiElement> {
        override fun resolve(ref: ParadoxLocalisationParameterPsiReference, incompleteCode: Boolean) = ref.doResolve()
    }

    private object MultiResolver : ResolveCache.PolyVariantResolver<ParadoxLocalisationParameterPsiReference> {
        override fun resolve(ref: ParadoxLocalisationParameterPsiReference, incompleteCode: Boolean) = ref.doMultiResolve()
    }

    private object LocResolver : ResolveCache.AbstractResolver<ParadoxLocalisationParameterPsiReference, PsiElement> {
        override fun resolve(ref: ParadoxLocalisationParameterPsiReference, incompleteCode: Boolean) = ref.doResolve(onlyLocalisation = true)
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
        val type = ParadoxLocalisationType.resolve(file) ?: return null
        val locale = selectLocale(file)
        val name = element.name

        //尝试解析成localisation或者synced_localisation
        val selector = selector(project, file).localisation().contextSensitive().preferLocale(locale)
        val resolved = when (type) {
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
        val type = ParadoxLocalisationType.resolve(file) ?: return emptyArray()
        val locale = selectLocale(file)
        val name = element.name

        //尝试解析成localisation或者synced_localisation
        val selector = selector(project, file).localisation().contextSensitive().preferLocale(locale)
        val resolved = when (type) {
            Normal -> ParadoxLocalisationSearch.search(name, selector).findAll() //查找所有语言环境的
            Synced -> ParadoxSyncedLocalisationSearch.search(name, selector).findAll() //查找所有语言环境的
        }
        if (resolved.isNotEmpty()) return resolved.mapToArray { PsiElementResolveResult(it) }
        if (onlyLocalisation) return ResolveResult.EMPTY_ARRAY

        //尝试解析成localisation_parameter
        val resolvedParameter = ParadoxLocalisationParameterSupport.resolveParameter(element)
        if (resolvedParameter != null) return resolvedParameter.let { arrayOf(PsiElementResolveResult(it)) }

        return ResolveResult.EMPTY_ARRAY
    }
}
