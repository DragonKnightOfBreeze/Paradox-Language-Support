package icu.windea.pls.ep.documentation

import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxBaseDefinitionExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentationContent(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        val definitionName = definitionInfo.name
        if (definitionName.isEmpty()) return null
        if (definitionName.isParameterized()) return null
        val configGroup = definitionInfo.configGroup
        val configs = configGroup.extendedDefinitions.findFromPattern(definitionName, definition, configGroup).orEmpty()
        val config = configs.findLast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) } ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}

class ParadoxGameRuleExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentationContent(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        val definitionName = definitionInfo.name
        if (definitionName.isEmpty()) return null
        if (definitionName.isParameterized()) return null
        if (definitionInfo.type != "game_rule") return null
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedGameRules.findFromPattern(definitionName, definition, configGroup) ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}

class ParadoxOnActionExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentationContent(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        val definitionName = definitionInfo.name
        if (definitionName.isEmpty()) return null
        if (definitionName.isParameterized()) return null
        if (definitionInfo.type != "on_action") return null
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedOnActions.findFromPattern(definitionInfo.name, definition, configGroup) ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}

class ParadoxTextColorExtendedDocumentationProvider : ParadoxDefinitionExtendedDocumentationProvider {
    override fun getDocumentationContent(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String? {
        val definitionName = definitionInfo.name
        if (definitionName.isEmpty()) return null
        if (definitionName.isParameterized()) return null
        if (definitionInfo.type != "text_color") return null
        val info = ParadoxTextColorManager.getInfo(definition) ?: return null
        return info.textWithColor
    }
}
