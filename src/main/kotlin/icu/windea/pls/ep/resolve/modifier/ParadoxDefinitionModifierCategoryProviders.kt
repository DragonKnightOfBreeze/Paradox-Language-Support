package icu.windea.pls.ep.resolve.modifier

import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.util.ParadoxScriptedModifierManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * （仅限 Stellaris）适用于封装修正（`scripted_modifier`）。
 */
@ForGameType(ParadoxGameType.Stellaris)
class ParadoxScriptedModifierCategoryProvider : ParadoxDefinitionModifierCategoryProvider {
    override fun supports(gameType: ParadoxGameType) = gameType == ParadoxGameType.Stellaris

    override fun getModifierCategories(definition: ParadoxDefinitionElement): Map<String, CwtModifierCategoryConfig>? {
        val definitionInfo = definition.definitionInfo ?: return null
        if (definitionInfo.type != "scripted_modifier") return null
        return ParadoxScriptedModifierManager.getModifierCategory(definition, definitionInfo)
    }

    override fun getModifierCategories(definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>? {
        val definition = definitionInfo.element ?: return null
        if (definitionInfo.type != "scripted_modifier") return null
        return ParadoxScriptedModifierManager.getModifierCategory(definition, definitionInfo)
    }
}
