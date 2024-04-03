package icu.windea.pls.ep.documentation

import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.script.psi.*

class ParadoxBaseDefinitionExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        val name = definitionInfo.name
        if(name.isEmpty()) return null
        if(name.isParameterized()) return null
        val configGroup = definitionInfo.configGroup
        val configs = configGroup.extendedDefinitions.getAllByTemplate(name, definition, configGroup)
        val config = configs.findLast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) } ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}

class ParadoxGameRuleExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        val name = definitionInfo.name
        if(name.isEmpty()) return null
        if(name.isParameterized()) return null
        if(definitionInfo.type != "game_rule") return null
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedGameRules.getByTemplate(name, definition, configGroup) ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}

class ParadoxOnActionExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        val name = definitionInfo.name
        if(name.isEmpty()) return null
        if(name.isParameterized()) return null
        if(definitionInfo.type != "on_action") return null
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedOnActions.getByTemplate(definitionInfo.name, definition, configGroup) ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}