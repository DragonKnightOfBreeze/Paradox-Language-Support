package icu.windea.pls.ep.codeInsight.documentation

import icu.windea.pls.PlsFacade
import icu.windea.pls.config.documentation
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.match.matchesByPattern
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression
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
    override val source: ParadoxQuickDocTextProvider.Source get() = ParadoxQuickDocTextProvider.Source.Extended

    override fun doGetQuickDocText(element: ParadoxScriptScriptedVariable, name: String): String? {
        val gameType = selectGameType(element) ?: return null
        val project = element.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val config = configGroup.extendedScriptedVariables.findByPattern(name, element, configGroup) ?: return null
        val quickDoc = config.config.documentation?.orNull()
        return quickDoc
    }
}

class ParadoxExtendedDefinitionQuickDocTextProvider : ParadoxQuickDocTextProviderBase.Definition() {
    override val source: ParadoxQuickDocTextProvider.Source get() = ParadoxQuickDocTextProvider.Source.Extended

    override fun doGetQuickDocText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        val name = definitionInfo.name
        val configGroup = definitionInfo.configGroup
        val configs = configGroup.extendedDefinitions.findByPattern(name, element, configGroup).orEmpty()
        val config = configs.findLast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) } ?: return null
        val quickDoc = config.config.documentation?.orNull()
        return quickDoc
    }
}

class ParadoxExtendedGameRuleQuickDocTextProvider : ParadoxQuickDocTextProviderBase.Definition() {
    override val source: ParadoxQuickDocTextProvider.Source get() = ParadoxQuickDocTextProvider.Source.Extended

    override fun doGetQuickDocText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        if (definitionInfo.type != ParadoxDefinitionTypes.GameRule) return null
        val name = definitionInfo.name
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedGameRules.findByPattern(name, element, configGroup) ?: return null
        val quickDoc = config.config.documentation?.orNull()
        return quickDoc
    }
}

class ParadoxExtendedOnActionQuickDocTextProvider : ParadoxQuickDocTextProviderBase.Definition() {
    override val source: ParadoxQuickDocTextProvider.Source get() = ParadoxQuickDocTextProvider.Source.Extended

    override fun doGetQuickDocText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        if (definitionInfo.type != ParadoxDefinitionTypes.OnAction) return null
        val name = definitionInfo.name
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedOnActions.findByPattern(name, element, configGroup) ?: return null
        val quickDoc = config.config.documentation?.orNull()
        return quickDoc
    }
}

class ParadoxExtendedComplexEnumValueQuickDocTextProvider : ParadoxQuickDocTextProviderBase.ComplexEnumValue() {
    override val source: ParadoxQuickDocTextProvider.Source get() = ParadoxQuickDocTextProvider.Source.Extended

    override fun doGetQuickDocText(element: ParadoxComplexEnumValueElement): String? {
        val name = element.name
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        val configs = configGroup.extendedComplexEnumValues[element.enumName] ?: return null
        val config = configs.findByPattern(name, element, configGroup) ?: return null
        val quickDoc = config.config.documentation?.orNull()
        return quickDoc
    }
}

class ParadoxExtendedDynamicValueQuickDocTextProvider : ParadoxQuickDocTextProviderBase.DynamicValue() {
    override val source: ParadoxQuickDocTextProvider.Source get() = ParadoxQuickDocTextProvider.Source.Extended

    override fun doGetQuickDocText(element: ParadoxDynamicValueElement): String? {
        val name = element.name
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        for (type in element.dynamicValueTypes) {
            val configs = configGroup.extendedDynamicValues[type] ?: continue
            val config = configs.findByPattern(name, element, configGroup) ?: continue
            val quickDoc = config.config.documentation?.orNull()
            if (quickDoc != null) return quickDoc
        }
        return null
    }
}

class ParadoxExtendedParameterQuickDocTextProvider : ParadoxQuickDocTextProviderBase.Parameter() {
    override val source: ParadoxQuickDocTextProvider.Source get() = ParadoxQuickDocTextProvider.Source.Extended

    override fun doGetQuickDocText(element: ParadoxParameterElement): String? {
        val name = element.name
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        val configs = configGroup.extendedParameters.findByPattern(name, element, configGroup).orEmpty()
        val config = configs.findLast { it.contextKey.matchesByPattern(element.contextKey, element, configGroup) } ?: return null
        val quickDoc = config.config.documentation?.orNull()
        return quickDoc
    }
}
