package icu.windea.pls.lang.documentation

import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxBaseDefinitionExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
        val gameType = definitionInfo.gameType
        val documentation = PlsDocumentationBundle.message(gameType, definitionInfo.name, definitionInfo.type)
        return documentation
    }
}

class ParadoxGameRuleExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        if(definitionInfo.type != "game_rule") return null
        if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
        val config = definitionInfo.configGroup.gameRules.get(definitionInfo.name)
        val documentation = config?.config?.documentation?.orNull()
        return documentation
    }
}

class ParadoxOnActionExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        if(definitionInfo.type != "on_action") return null
        if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
        val config = definitionInfo.configGroup.onActions.getByTemplate(definitionInfo.name, definition, definitionInfo.configGroup)
        val documentation = config?.config?.documentation?.orNull()
        return documentation
    }
}