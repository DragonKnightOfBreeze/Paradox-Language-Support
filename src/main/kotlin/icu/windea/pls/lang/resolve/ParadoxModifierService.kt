package icu.windea.pls.lang.resolve

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
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

    // region Related Items

    /**
     * 解析修正关联的所有本地化（包括名字和描述）。
     *
     * 备注：名字和描述各自仅使用第一个能够解析到本地化的键。
     *
     * @param nameKeys 修正的名字对应的本地化键。
     * @param descKeys 修正的描述对应的本地化键。
     */
    fun resolveRelatedLocalisations(
        nameKeys: Set<String>,
        descKeys: Set<String>,
        contextElement: PsiElement,
        preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig(),
        preferred: Boolean = false,
    ): List<ParadoxLocalisationProperty> {
        if (nameKeys.isEmpty() && descKeys.isEmpty()) return emptyList()
        val result = mutableListOf<ParadoxLocalisationProperty>()
        result.addAll(resolveRelatedLocalisationsFrom(nameKeys, contextElement, preferredLocale, preferred))
        result.addAll(resolveRelatedLocalisationsFrom(descKeys, contextElement, preferredLocale, preferred))
        return result
    }

    /**
     * 解析 [keys] 对应的所有相关本地化。
     *
     * 说明：
     * - 仅使用第一个能够解析到本地化的键。
     * - 使用特殊的索引约束（[ParadoxLocalisationIndexConstraint.Modifier]）。
     */
    fun resolveRelatedLocalisationsFrom(
        keys: Set<String>,
        contextElement: PsiElement,
        preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig(),
        preferred: Boolean = false,
    ): List<ParadoxLocalisationProperty> {
        if (keys.isEmpty()) return emptyList()
        val project = contextElement.project
        val result = mutableListOf<ParadoxLocalisationProperty>()
        for (key in keys) {
            if (key.isEmpty()) continue
            ProgressManager.checkCanceled()
            val selector = ParadoxLocalisationSearch.selector(project, contextElement).contextSensitive().preferLocale(preferredLocale)
                .withConstraint(ParadoxLocalisationIndexConstraint.Modifier) // so ignore case
            val query = ParadoxLocalisationSearch.searchNormal(key, selector)
            if (preferred) query.find()?.let { result += it } else query.findAll().let { result += it }
            if (result.isNotEmpty()) return result
        }
        return emptyList()
    }

    // endregion
}
