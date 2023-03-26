package icu.windea.pls.lang.scope

import icu.windea.pls.config.config.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisScriptedModifierSupportedScopesProvider : ParadoxDefinitionSupportedScopesProvider {
    override fun getSupportedScopes(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>? {
        if(definitionInfo.type != "scripted_modifier") return null
        val modifierCategory = StellarisScriptedModifierHandler.resolveModifierCategory(definition, definitionInfo) ?: return null
        return modifierCategory.getSupportedScopes()
    }
}