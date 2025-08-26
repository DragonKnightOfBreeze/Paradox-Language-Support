package icu.windea.pls.ep.codeInsight.hints

import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

class ParadoxExtendedScriptedVariableQuickDocTextProvider : ParadoxQuickDocTextProviderBase.ScriptedVariable() {
    override fun doGetQuickDocText(element: ParadoxScriptScriptedVariable, name: String): String? {
        val gameType = selectGameType(element) ?: return null
        val project = element.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val config = configGroup.extendedScriptedVariables.findFromPattern(name, element, configGroup) ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}

class ParadoxExtendedDefinitionQuickDocTextProvider : ParadoxQuickDocTextProviderBase.Definition() {
    override fun doGetQuickDocText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        val name = definitionInfo.name
        val configGroup = definitionInfo.configGroup
        val configs = configGroup.extendedDefinitions.findFromPattern(name, element, configGroup).orEmpty()
        val config = configs.findLast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) } ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}

class ParadoxExtendedGameRuleQuickDocTextProvider : ParadoxQuickDocTextProviderBase.Definition() {
    override fun doGetQuickDocText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        if (definitionInfo.type != ParadoxDefinitionTypes.GameRule) return null
        val name = definitionInfo.name
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedGameRules.findFromPattern(name, element, configGroup) ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}

class ParadoxExtendedOnActionQuickDocTextProvider : ParadoxQuickDocTextProviderBase.Definition() {
    override fun doGetQuickDocText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        if (definitionInfo.type != ParadoxDefinitionTypes.OnAction) return null
        val name = definitionInfo.name
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedGameRules.findFromPattern(name, element, configGroup) ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}

class ParadoxExtendedTextColorQuickDocTextProvider : ParadoxQuickDocTextProviderBase.Definition() {
    override fun doGetQuickDocText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        if (definitionInfo.type != ParadoxDefinitionTypes.TextColor) return null
        val info = ParadoxTextColorManager.getInfo(element) ?: return null
        return info.textWithColor
    }
}

class ParadoxExtendedComplexEnumValueQuickDocTextProvider : ParadoxQuickDocTextProviderBase.ComplexEnumValue() {
    override fun doGetQuickDocText(element: ParadoxComplexEnumValueElement): String? {
        val name = element.name
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        val configs = configGroup.extendedComplexEnumValues[element.enumName] ?: return null
        val config = configs.findFromPattern(name, element, configGroup) ?: return null
        val doc = config.config.documentation?.orNull()
        return doc
    }
}

class ParadoxExtendedDynamicValueQuickDocTextProvider : ParadoxQuickDocTextProviderBase.DynamicValue() {
    override fun doGetQuickDocText(element: ParadoxDynamicValueElement): String? {
        val name = element.name
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        for (type in element.dynamicValueTypes) {
            val configs = configGroup.extendedDynamicValues[type] ?: continue
            val config = configs.findFromPattern(name, element, configGroup) ?: continue
            val documentation = config.config.documentation?.orNull()
            if (documentation != null) return documentation
        }
        return null
    }
}

class ParadoxExtendedParameterQuickDocTextProvider : ParadoxQuickDocTextProviderBase.Parameter() {
    override fun doGetQuickDocText(element: ParadoxParameterElement): String? {
        val name = element.name
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        val configs = configGroup.extendedParameters.findFromPattern(name, element, configGroup).orEmpty()
        val config = configs.findLast { it.contextKey.matchFromPattern(element.contextKey, element, configGroup) } ?: return null
        val documentation = config.config.documentation?.orNull()
        return documentation
    }
}
