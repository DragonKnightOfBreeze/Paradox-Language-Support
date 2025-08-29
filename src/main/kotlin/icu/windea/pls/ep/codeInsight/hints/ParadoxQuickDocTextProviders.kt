package icu.windea.pls.ep.codeInsight.hints

import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.extendedComplexEnumValues
import icu.windea.pls.config.configGroup.extendedDefinitions
import icu.windea.pls.config.configGroup.extendedDynamicValues
import icu.windea.pls.config.configGroup.extendedGameRules
import icu.windea.pls.config.configGroup.extendedParameters
import icu.windea.pls.config.configGroup.extendedScriptedVariables
import icu.windea.pls.config.documentation
import icu.windea.pls.config.findFromPattern
import icu.windea.pls.config.matchFromPattern
import icu.windea.pls.core.orNull
import icu.windea.pls.ep.codeInsight.hints.ParadoxQuickDocTextProvider.Source
import icu.windea.pls.lang.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxTextColorManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ParadoxTextColorQuickDocTextProvider : ParadoxQuickDocTextProviderBase.Definition() {
    override fun doGetQuickDocText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        if (definitionInfo.type != ParadoxDefinitionTypes.TextColor) return null
        val info = ParadoxTextColorManager.getInfo(element) ?: return null
        val quickDoc = info.textWithColor
        return quickDoc
    }
}

class ParadoxExtendedScriptedVariableQuickDocTextProvider : ParadoxQuickDocTextProviderBase.ScriptedVariable() {
    override val source: Source get() = Source.Extended

    override fun doGetQuickDocText(element: ParadoxScriptScriptedVariable, name: String): String? {
        val gameType = selectGameType(element) ?: return null
        val project = element.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val config = configGroup.extendedScriptedVariables.findFromPattern(name, element, configGroup) ?: return null
        val quickDoc = config.config.documentation?.orNull()
        return quickDoc
    }
}

class ParadoxExtendedDefinitionQuickDocTextProvider : ParadoxQuickDocTextProviderBase.Definition() {
    override val source: Source get() = Source.Extended

    override fun doGetQuickDocText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        val name = definitionInfo.name
        val configGroup = definitionInfo.configGroup
        val configs = configGroup.extendedDefinitions.findFromPattern(name, element, configGroup).orEmpty()
        val config = configs.findLast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) } ?: return null
        val quickDoc = config.config.documentation?.orNull()
        return quickDoc
    }
}

class ParadoxExtendedGameRuleQuickDocTextProvider : ParadoxQuickDocTextProviderBase.Definition() {
    override val source: Source get() = Source.Extended

    override fun doGetQuickDocText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        if (definitionInfo.type != ParadoxDefinitionTypes.GameRule) return null
        val name = definitionInfo.name
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedGameRules.findFromPattern(name, element, configGroup) ?: return null
        val quickDoc = config.config.documentation?.orNull()
        return quickDoc
    }
}

class ParadoxExtendedOnActionQuickDocTextProvider : ParadoxQuickDocTextProviderBase.Definition() {
    override val source: Source get() = Source.Extended

    override fun doGetQuickDocText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        if (definitionInfo.type != ParadoxDefinitionTypes.OnAction) return null
        val name = definitionInfo.name
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedGameRules.findFromPattern(name, element, configGroup) ?: return null
        val quickDoc = config.config.documentation?.orNull()
        return quickDoc
    }
}

class ParadoxExtendedComplexEnumValueQuickDocTextProvider : ParadoxQuickDocTextProviderBase.ComplexEnumValue() {
    override val source: Source get() = Source.Extended

    override fun doGetQuickDocText(element: ParadoxComplexEnumValueElement): String? {
        val name = element.name
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        val configs = configGroup.extendedComplexEnumValues[element.enumName] ?: return null
        val config = configs.findFromPattern(name, element, configGroup) ?: return null
        val quickDoc = config.config.documentation?.orNull()
        return quickDoc
    }
}

class ParadoxExtendedDynamicValueQuickDocTextProvider : ParadoxQuickDocTextProviderBase.DynamicValue() {
    override val source: Source get() = Source.Extended

    override fun doGetQuickDocText(element: ParadoxDynamicValueElement): String? {
        val name = element.name
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        for (type in element.dynamicValueTypes) {
            val configs = configGroup.extendedDynamicValues[type] ?: continue
            val config = configs.findFromPattern(name, element, configGroup) ?: continue
            val quickDoc = config.config.documentation?.orNull()
            if (quickDoc != null) return quickDoc
        }
        return null
    }
}

class ParadoxExtendedParameterQuickDocTextProvider : ParadoxQuickDocTextProviderBase.Parameter() {
    override val source: Source get() = Source.Extended

    override fun doGetQuickDocText(element: ParadoxParameterElement): String? {
        val name = element.name
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        val configs = configGroup.extendedParameters.findFromPattern(name, element, configGroup).orEmpty()
        val config = configs.findLast { it.contextKey.matchFromPattern(element.contextKey, element, configGroup) } ?: return null
        val quickDoc = config.config.documentation?.orNull()
        return quickDoc
    }
}
