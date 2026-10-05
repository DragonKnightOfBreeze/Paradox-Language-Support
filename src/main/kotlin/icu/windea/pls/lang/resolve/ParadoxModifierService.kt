package icu.windea.pls.lang.resolve

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.CaseInsensitiveStringSet
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.processFast
import icu.windea.pls.core.optimizedIfEmpty
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierIconProvider
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierNameDescProvider
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierSupport
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.index.constraints.ParadoxLocalisationIndexConstraint
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.search.util.withConstraint
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxModifierInfo
import icu.windea.pls.model.orSpecific
import icu.windea.pls.model.support

@Optimized
object ParadoxModifierService {
    /**
     * @see ParadoxModifierSupport.matchesModifier
     */
    fun matchesModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val gameType = configGroup.gameType
        val supports = ParadoxModifierSupport.EP_NAME.extensionList
        return supports.anyFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f false // check game type first
            ep.matchesModifier(name, element, configGroup)
        }
    }

    /**
     * @see ParadoxModifierSupport.resolveModifier
     */
    fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
        val gameType = configGroup.gameType
        val supports = ParadoxModifierSupport.EP_NAME.extensionList
        supports.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.resolveModifier(name, element, configGroup)?.also { it.support = ep }?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxModifierSupport.completeModifier
     */
    fun completeModifier(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val gameType = context.gameType
        val modifierNames = CaseInsensitiveStringSet() // 3.0.1 clarify: ignore case (for modifier names)
        val supports = ParadoxModifierSupport.EP_NAME.extensionList
        supports.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.completeModifier(context, result, modifierNames)
        }
    }

    /**
     * @see ParadoxModifierSupport.processModifier
     */
    fun processModifier(element: PsiElement, configGroup: CwtConfigGroup, processor: Processor<ParadoxModifierLightElement>): Boolean {
        val gameType = configGroup.gameType
        val supports = ParadoxModifierSupport.EP_NAME.extensionList
        return supports.processFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f true // check game type first
            ep.processModifier(element, configGroup, processor)
        }
    }

    /**
     * @see ParadoxModifierIconProvider.addModifierIconPath
     */
    fun getModifierIconPaths(element: PsiElement, modifierInfo: ParadoxModifierInfo): Set<String> {
        val gameType = modifierInfo.gameType
        val eps = ParadoxModifierIconProvider.EP_NAME.extensionList
        val result = mutableSetOf<String>() // 3.0.1 do not use `CaseInsensitiveStringSet` here
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.addModifierIconPath(modifierInfo, element, result)
        }
        return result.optimizedIfEmpty()
    }

    /**
     * @see ParadoxModifierNameDescProvider.addModifierNameKey
     */
    fun getModifierNameKeys(element: PsiElement, modifierInfo: ParadoxModifierInfo): Set<String> {
        val gameType = modifierInfo.gameType
        val result = mutableSetOf<String>() // 3.0.1 do not use `CaseInsensitiveStringSet` here
        val eps = ParadoxModifierNameDescProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.addModifierNameKey(modifierInfo, element, result)
        }
        return result.optimizedIfEmpty()
    }

    /**
     * @see ParadoxModifierNameDescProvider.addModifierDescKey
     */
    fun getModifierDescKeys(element: PsiElement, modifierInfo: ParadoxModifierInfo): Set<String> {
        val gameType = modifierInfo.gameType
        val result = mutableSetOf<String>() // 3.0.1 do not use `CaseInsensitiveStringSet` here
        val eps = ParadoxModifierNameDescProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.addModifierDescKey(modifierInfo, element, result)
        }
        return result.optimizedIfEmpty()
    }

    /**
     * 解析修正关联的（第一个）名字本地化。
     *
     * @param nameKeys 修正的名字对应的本地化键。
     */
    /**
     * 解析修正关联的名字本地化。仅使用第一个能够解析到本地化的名字键。
     *
     * @param nameKeys 修正的名字对应的本地化键。
     */
    fun resolveRelatedNameLocalisations(nameKeys: Set<String>, contextElement: PsiElement, project: Project): List<ParadoxLocalisationProperty> {
        if (nameKeys.isEmpty()) return emptyList()
        ProgressManager.checkCanceled()
        val selector = ParadoxLocalisationSearch.selector(project, contextElement).contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            .withConstraint(ParadoxLocalisationIndexConstraint.Modifier) // so ignore case
        for (key in nameKeys) {
            val localisations = ParadoxLocalisationSearch.searchNormal(key, selector).findAll()
            if (localisations.isNotEmpty()) return localisations
        }
        return emptyList()
    }

    /**
     * 解析修正关联的（第一个）名字本地化。
     *
     * @param nameKeys 修正的名字对应的本地化键。
     */
    fun resolveRelatedLocalisation(nameKeys: Set<String>, contextElement: PsiElement, project: Project): ParadoxLocalisationProperty? {
        return resolveRelatedNameLocalisations(nameKeys, contextElement, project).firstOrNull()
    }

    /**
     * 解析修正关联的所有本地化（包括名字和描述）。
     *
     * 备注：名字和描述各自仅使用第一个能够解析到本地化的键。
     *
     * @param nameKeys 修正的名字对应的本地化键。
     * @param descKeys 修正的描述对应的本地化键。
     */
    fun resolveRelatedLocalisations(nameKeys: Set<String>, descKeys: Set<String>, contextElement: PsiElement, project: Project): List<ParadoxLocalisationProperty> {
        if (nameKeys.isEmpty() && descKeys.isEmpty()) return emptyList()
        val result = mutableListOf<ParadoxLocalisationProperty>()
        result.addAll(resolveRelatedNameLocalisations(nameKeys, contextElement, project))
        result.addAll(resolveRelatedNameLocalisations(descKeys, contextElement, project))
        return result
    }
}
