package icu.windea.pls.lang.resolve

import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.ep.resolve.modifier.ParadoxDefinitionModifierCategoryProvider
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierCategoryProvider
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxModifierInfo
import icu.windea.pls.model.orSpecific
import icu.windea.pls.script.psi.ParadoxDefinitionElement

object ParadoxModifierCategoryService {
    /**
     * @see ParadoxModifierCategoryProvider.getModifierCategories
     */
    fun getModifierCategories(modifier: ParadoxModifierLightElement): Map<String, CwtModifierCategoryConfig>? {
        val gameType = modifier.gameType
        val supports = ParadoxModifierCategoryProvider.EP_NAME.extensionList
        supports.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.getModifierCategories(modifier)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxModifierCategoryProvider.getModifierCategories
     */
    @Suppress("unused")
    fun getModifierCategories(modifierInfo: ParadoxModifierInfo): Map<String, CwtModifierCategoryConfig>? {
        val gameType = modifierInfo.gameType
        val supports = ParadoxModifierCategoryProvider.EP_NAME.extensionList
        supports.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.getModifierCategories(modifierInfo)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxDefinitionModifierCategoryProvider.getModifierCategories
     */
    @Suppress("unused")
    fun getModifierCategories(definition: ParadoxDefinitionElement): Map<String, CwtModifierCategoryConfig>? {
        val gameType = selectGameType(definition)
        val eps = ParadoxDefinitionModifierCategoryProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.getModifierCategories(definition)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxDefinitionModifierCategoryProvider.getModifierCategories
     */
    fun getModifierCategories(definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>? {
        val gameType = definitionInfo.gameType
        val eps = ParadoxDefinitionModifierCategoryProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.getModifierCategories(definitionInfo)?.let { return it }
        }
        return null
    }
}
