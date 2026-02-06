package icu.windea.pls.lang.resolve

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.codeInsight.documentation.DocumentationBuilder
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierIconProvider
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierNameDescProvider
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierSupport
import icu.windea.pls.ep.resolve.modifier.support
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.codeInsight.completion.gameType
import icu.windea.pls.lang.psi.mock.ParadoxModifierElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxModifierInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement

object ParadoxModifierService {
    /**
     * @see ParadoxModifierSupport.matchModifier
     */
    fun matchesModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val gameType = configGroup.gameType
        return ParadoxModifierSupport.EP_NAME.extensionList.any f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f false
            ep.matchModifier(name, element, configGroup)
        }
    }

    /**
     * @see ParadoxModifierSupport.resolveModifier
     */
    fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
        val gameType = configGroup.gameType
        return ParadoxModifierSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.resolveModifier(name, element, configGroup)?.also { it.support = ep }
        }
    }

    /**
     * @see ParadoxModifierSupport.completeModifier
     */
    fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
        val gameType = context.gameType ?: return
        ParadoxModifierSupport.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            ep.completeModifier(context, result, modifierNames)
        }
    }

    /**
     * @see ParadoxModifierSupport.getModifierCategories
     */
    fun getModifierCategories(element: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
        val gameType = element.gameType
        return ParadoxModifierSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getModifierCategories(element)
        }
    }

    /**
     * @see ParadoxModifierSupport.buildDocumentationDefinition
     */
    fun getDocumentationDefinition(element: ParadoxModifierElement, builder: DocumentationBuilder): Boolean {
        val gameType = element.gameType
        return ParadoxModifierSupport.EP_NAME.extensionList.any f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f false
            ep.buildDocumentationDefinition(element, builder)
        }
    }

    /**
     * @see ParadoxModifierSupport.buildDDocumentationDefinitionForDefinition
     */
    fun buildDDocumentationDefinitionForDefinition(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean {
        val gameType = definitionInfo.gameType
        return ParadoxModifierSupport.EP_NAME.extensionList.any f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f false
            ep.buildDDocumentationDefinitionForDefinition(definition, definitionInfo, builder)
        }
    }

    /**
     * @see ParadoxModifierIconProvider.addModifierIconPath
     */
    fun getModifierIconPaths(element: PsiElement, modifierInfo: ParadoxModifierInfo): Set<String> {
        val gameType = modifierInfo.gameType
        return buildSet {
            ParadoxModifierIconProvider.EP_NAME.extensionList.forEach f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f
                ep.addModifierIconPath(modifierInfo, element, this)
            }
        }
    }

    /**
     * @see ParadoxModifierNameDescProvider.addModifierNameKey
     */
    fun getModifierNameKeys(element: PsiElement, modifierInfo: ParadoxModifierInfo): Set<String> {
        val gameType = modifierInfo.gameType
        return buildSet {
            ParadoxModifierNameDescProvider.EP_NAME.extensionList.forEach f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f
                ep.addModifierNameKey(modifierInfo, element, this)
            }
        }
    }

    /**
     * @see ParadoxModifierNameDescProvider.addModifierDescKey
     */
    fun getModifierDescKeys(element: PsiElement, modifierInfo: ParadoxModifierInfo): Set<String> {
        val gameType = modifierInfo.gameType
        return buildSet {
            ParadoxModifierNameDescProvider.EP_NAME.extensionList.forEach f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f
                ep.addModifierDescKey(modifierInfo, element, this)
            }
        }
    }
}
