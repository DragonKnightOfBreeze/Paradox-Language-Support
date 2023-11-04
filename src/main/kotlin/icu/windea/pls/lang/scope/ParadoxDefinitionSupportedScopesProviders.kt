package icu.windea.pls.lang.scope

import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisScriptedModifierSupportedScopesProvider : ParadoxDefinitionSupportedScopesProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "scripted_modifier"
    }
    
    override fun getSupportedScopes(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>? {
        val modifierCategory = ParadoxScriptedModifierHandler.Stellaris.resolveModifierCategory(definition, definitionInfo) ?: return null
        return modifierCategory.getSupportedScopes()
    }
}
