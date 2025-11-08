package icu.windea.pls.ep.resolve.modifier

import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.util.ParadoxScriptedModifierManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

@WithGameType(ParadoxGameType.Stellaris)
class StellarisScriptedModifierDefinitionModifierProvider : ParadoxDefinitionModifierProvider {
    override fun getModifierCategories(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>? {
        if (definitionInfo.type != "scripted_modifier") return null
        return ParadoxScriptedModifierManager.resolveModifierCategory(definition, definitionInfo)
    }
}
