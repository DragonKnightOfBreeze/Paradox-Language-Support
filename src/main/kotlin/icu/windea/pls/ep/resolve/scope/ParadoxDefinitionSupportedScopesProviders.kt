package icu.windea.pls.ep.resolve.scope

import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.lang.util.ParadoxScriptedModifierManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType

@ForGameType(ParadoxGameType.Stellaris)
class StellarisScriptedModifierSupportedScopesProvider : ParadoxDefinitionSupportedScopesProvider {
    override fun supports(gameType: ParadoxGameType) = gameType == ParadoxGameType.Stellaris

    override fun supports(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "scripted_modifier"
    }

    override fun getSupportedScopes(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String> {
        val modifierCategory = ParadoxScriptedModifierManager.getModifierCategory(definition, definitionInfo)
        return ParadoxScopeManager.getSupportedScopes(modifierCategory)
    }
}
