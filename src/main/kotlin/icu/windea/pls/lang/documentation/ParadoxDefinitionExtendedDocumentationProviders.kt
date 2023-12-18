package icu.windea.pls.lang.documentation

import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxBaseDefinitionExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
        val configs = definitionInfo.configGroup.definitions.getAllByTemplate(definitionInfo.name, definition, definitionInfo.configGroup)
        val config = configs.findLast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) } ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}

class ParadoxGameRuleExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        if(definitionInfo.type != "game_rule") return null
        if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
        val config = definitionInfo.configGroup.gameRules.getByTemplate(definitionInfo.name, definition, definitionInfo.configGroup) ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}

class ParadoxOnActionExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        if(definitionInfo.type != "on_action") return null
        if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
        val config = definitionInfo.configGroup.onActions.getByTemplate(definitionInfo.name, definition, definitionInfo.configGroup) ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}