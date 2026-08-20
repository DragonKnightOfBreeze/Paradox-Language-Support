package icu.windea.pls.lang.codeInsight.documentation

import com.intellij.psi.PsiElement
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.forEachReversedFast
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.builders.DocumentationBuilder
import icu.windea.pls.ep.codeInsight.documentation.ParadoxLocalisationParameterQuickDocProvider
import icu.windea.pls.ep.codeInsight.documentation.ParadoxModifierQuickDocProvider
import icu.windea.pls.ep.codeInsight.documentation.ParadoxParameterQuickDocProvider
import icu.windea.pls.ep.codeInsight.documentation.ParadoxQuickDocTextProvider
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.orSpecific

object ParadoxDocumentationService {
    /**
     * @see ParadoxQuickDocTextProvider.getQuickDocText
     */
    @Suppress("unused")
    fun getQuickDocText(element: PsiElement): String? {
        val gameType = selectGameType(element)
        val eps = ParadoxQuickDocTextProvider.EP_NAME.extensionList
        eps.forEachReversedFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.getQuickDocText(element)?.orNull()?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxQuickDocTextProvider.getQuickDocText
     */
    fun listQuickDocText(element: PsiElement): List<String> {
        val gameType = selectGameType(element)
        val result = mutableListOf<String>()
        val eps = ParadoxQuickDocTextProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.getQuickDocText(element)?.orNull()?.let { result.add(it) }
        }
        return result
    }

    /**
     * @see ParadoxModifierQuickDocProvider.buildDefinitionPart
     */
    fun buildDefinitionPart(element: ParadoxModifierLightElement, builder: DocumentationBuilder): Boolean {
        val gameType = element.gameType
        val supports = ParadoxModifierQuickDocProvider.EP_NAME.extensionList
        return supports.anyFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f false // check game type first
            ep.buildDefinitionPart(element, builder)
        }
    }

    /**
     * @see ParadoxModifierQuickDocProvider.buildDefinitionPartForDefinition
     */
    fun buildDefinitionPartForDefinition(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean {
        val gameType = definitionInfo.gameType
        val supports = ParadoxModifierQuickDocProvider.EP_NAME.extensionList
        return supports.anyFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f false // check game type first
            ep.buildDefinitionPartForDefinition(definition, definitionInfo, builder)
        }
    }

    /**
     * @see ParadoxParameterQuickDocProvider.buildDefinitionPart
     */
    fun buildDefinitionPart(parameterElement: ParadoxParameterLightElement, builder: DocumentationBuilder): Boolean {
        val supports = ParadoxParameterQuickDocProvider.EP_NAME.extensionList
        return supports.anyFast { support ->
            support.buildDefinitionPart(parameterElement, builder)
        }
    }

    /**
     * @see ParadoxLocalisationParameterQuickDocProvider.buildDefinitionPart
     */
    fun buildDefinitionPart(element: ParadoxLocalisationParameterLightElement, builder: DocumentationBuilder): Boolean {
        val supports = ParadoxLocalisationParameterQuickDocProvider.EP_NAME.extensionList
        return supports.anyFast { support ->
            support.buildDefinitionPart(element, builder)
        }
    }
}
