package icu.windea.pls.lang.resolve

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.PsiElement
import icu.windea.pls.base.annotations.ChronicleAnnotationService
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.text.DocumentationBuilder
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierIconProvider
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierNameDescProvider
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierSupport
import icu.windea.pls.ep.resolve.modifier.support
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxModifierInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement

@Optimized
object ParadoxModifierService {
    /**
     * @see ParadoxModifierSupport.matchModifier
     */
    fun matchesModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val gameType = configGroup.gameType
        val supports = ParadoxModifierSupport.EP_NAME.extensionList
        return supports.anyFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f false
            ep.matchModifier(name, element, configGroup)
        }
    }

    /**
     * @see ParadoxModifierSupport.resolveModifier
     */
    fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
        val gameType = configGroup.gameType
        val supports = ParadoxModifierSupport.EP_NAME.extensionList
        supports.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ep.resolveModifier(name, element, configGroup)?.also { it.support = ep }?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxModifierSupport.completeModifier
     */
    fun completeModifier(context: ParadoxCompletionContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
        val gameType = context.gameType
        val supports = ParadoxModifierSupport.EP_NAME.extensionList
        supports.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ep.completeModifier(context, result, modifierNames)
        }
    }

    /**
     * @see ParadoxModifierSupport.getModifierCategories
     */
    fun getModifierCategories(element: ParadoxModifierLightElement): Map<String, CwtModifierCategoryConfig>? {
        val gameType = element.gameType
        val supports = ParadoxModifierSupport.EP_NAME.extensionList
        supports.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ep.getModifierCategories(element)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxModifierSupport.buildDocumentationDefinition
     */
    fun getDocumentationDefinition(element: ParadoxModifierLightElement, builder: DocumentationBuilder): Boolean {
        val gameType = element.gameType
        val supports = ParadoxModifierSupport.EP_NAME.extensionList
        return supports.anyFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f false
            ep.buildDocumentationDefinition(element, builder)
        }
    }

    /**
     * @see ParadoxModifierSupport.buildDDocumentationDefinitionForDefinition
     */
    fun buildDDocumentationDefinitionForDefinition(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean {
        val gameType = definitionInfo.gameType
        val supports = ParadoxModifierSupport.EP_NAME.extensionList
        return supports.anyFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f false
            ep.buildDDocumentationDefinitionForDefinition(definition, definitionInfo, builder)
        }
    }

    /**
     * @see ParadoxModifierIconProvider.addModifierIconPath
     */
    fun getModifierIconPaths(element: PsiElement, modifierInfo: ParadoxModifierInfo): Set<String> {
        val gameType = modifierInfo.gameType
        val eps = ParadoxModifierIconProvider.EP_NAME.extensionList
        val result = mutableSetOf<String>()
        eps.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ep.addModifierIconPath(modifierInfo, element, result)
        }
        if (result.isEmpty()) return emptySet()
        return result
    }

    /**
     * @see ParadoxModifierNameDescProvider.addModifierNameKey
     */
    fun getModifierNameKeys(element: PsiElement, modifierInfo: ParadoxModifierInfo): Set<String> {
        val gameType = modifierInfo.gameType
        val eps = ParadoxModifierNameDescProvider.EP_NAME.extensionList
        val result = mutableSetOf<String>()
        eps.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ep.addModifierNameKey(modifierInfo, element, result)
        }
        if (result.isEmpty()) return emptySet()
        return result
    }

    /**
     * @see ParadoxModifierNameDescProvider.addModifierDescKey
     */
    fun getModifierDescKeys(element: PsiElement, modifierInfo: ParadoxModifierInfo): Set<String> {
        val gameType = modifierInfo.gameType
        val eps = ParadoxModifierNameDescProvider.EP_NAME.extensionList
        val result = mutableSetOf<String>()
        eps.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ep.addModifierDescKey(modifierInfo, element, result)
        }
        if (result.isEmpty()) return emptySet()
        return result
    }
}
