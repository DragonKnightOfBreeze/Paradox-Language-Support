package icu.windea.pls.ep.scope

import icu.windea.pls.core.annotations.WithGameType
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.lang.util.ParadoxScriptedModifierManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

@WithGameType(ParadoxGameType.Stellaris)
class StellarisScriptedModifierSupportedScopesProvider : ParadoxDefinitionSupportedScopesProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "scripted_modifier"
    }

    override fun getSupportedScopes(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>? {
        val modifierCategory = ParadoxScriptedModifierManager.resolveModifierCategory(definition, definitionInfo) ?: return null
        return ParadoxScopeManager.getSupportedScopes(modifierCategory)
    }
}
