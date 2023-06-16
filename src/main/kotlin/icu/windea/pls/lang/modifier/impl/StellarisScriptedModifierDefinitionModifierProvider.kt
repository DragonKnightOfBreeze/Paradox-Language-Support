package icu.windea.pls.lang.modifier.impl

import icu.windea.pls.config.config.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisScriptedModifierDefinitionModifierProvider : ParadoxDefinitionModifierProvider {
    override fun getModifierCategories(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>? {
        if(definitionInfo.type != "scripted_modifier") return null
        return ParadoxScriptedModifierHandler.Stellaris.resolveModifierCategory(definition, definitionInfo)
    }
}
