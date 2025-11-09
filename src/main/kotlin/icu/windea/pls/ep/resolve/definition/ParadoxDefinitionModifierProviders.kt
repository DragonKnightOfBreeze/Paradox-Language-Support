package icu.windea.pls.ep.resolve.definition

import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.util.ParadoxScriptedModifierManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType

@WithGameType(ParadoxGameType.Stellaris)
class StellarisScriptedModifierDefinitionModifierProvider : ParadoxDefinitionModifierProvider {
    override fun getModifierCategories(definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>? {
        if (definitionInfo.type != "scripted_modifier") return null
        val definition = definitionInfo.element
        return ParadoxScriptedModifierManager.resolveModifierCategory(definition, definitionInfo)
    }
}
