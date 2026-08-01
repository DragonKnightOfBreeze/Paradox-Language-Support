package icu.windea.pls.ep.resolve.definition

import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.lang.util.ParadoxScriptedModifierManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType

@ForGameType(ParadoxGameType.Stellaris)
class StellarisScriptedModifierDefinitionModifierProvider : ParadoxDefinitionModifierProvider {
    override fun supports(gameType: ParadoxGameType) = gameType == ParadoxGameType.Stellaris

    override fun getModifierCategories(definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>? {
        if (definitionInfo.type != "scripted_modifier") return null
        val definition = definitionInfo.element ?: return null
        return ParadoxScriptedModifierManager.resolveModifierCategory(definition, definitionInfo)
    }
}
