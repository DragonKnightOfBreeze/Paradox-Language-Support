package icu.windea.pls.ep.resolve.scope

import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.lang.util.ParadoxScriptedModifierManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxDefinitionElement

@WithGameType(ParadoxGameType.Stellaris)
class StellarisScriptedModifierSupportedScopesProvider : ParadoxDefinitionSupportedScopesProvider {
    override fun supports(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "scripted_modifier"
    }

    override fun getSupportedScopes(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>? {
        val modifierCategory = ParadoxScriptedModifierManager.resolveModifierCategory(definition, definitionInfo)
        return ParadoxScopeManager.getSupportedScopes(modifierCategory)
    }
}
