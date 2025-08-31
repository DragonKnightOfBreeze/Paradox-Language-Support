package icu.windea.pls.ep.codeInsight.hints

import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.configGroup.extendedComplexEnumValues
import icu.windea.pls.config.configGroup.extendedDefinitions
import icu.windea.pls.config.configGroup.extendedDynamicValues
import icu.windea.pls.config.configGroup.extendedGameRules
import icu.windea.pls.config.configGroup.extendedOnActions
import icu.windea.pls.config.configGroup.extendedScriptedVariables
import icu.windea.pls.config.findFromPattern
import icu.windea.pls.core.orNull
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider.Source
import icu.windea.pls.lang.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxDynamicValueManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ParadoxDefinitionHintTextProvider : ParadoxHintTextProviderBase.Definition() {
    override val source: Source get() = Source.PrimaryLocalisation

    override fun doGetHintText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, locale: CwtLocaleConfig?): String? {
        return doGetHintLocalisation(element, definitionInfo, locale)?.value?.orNull()
    }

    override fun doGetHintLocalisation(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
        return ParadoxDefinitionManager.getPrimaryLocalisation(element)
    }
}

class ParadoxInferredScriptedVariableHintTextProvider : ParadoxHintTextProviderBase.ScriptedVariable() {
    override val source: Source get() = Source.NameLocalisation

    override fun doGetHintText(element: ParadoxScriptScriptedVariable, name: String, locale: CwtLocaleConfig?): String? {
        return ParadoxScriptedVariableManager.getLocalizedName(element)
    }

    override fun doGetHintLocalisation(element: ParadoxScriptScriptedVariable, name: String, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
        val localeToUse = locale ?: ParadoxLocaleManager.getPreferredLocaleConfig()
        return ParadoxScriptedVariableManager.getNameLocalisation(name, element, localeToUse)
    }
}

class ParadoxInferredComplexEnumValueHintTextProvider : ParadoxHintTextProviderBase.ComplexEnumValue() {
    override val source: Source get() = Source.NameLocalisation

    override fun doGetHintText(element: ParadoxComplexEnumValueElement, locale: CwtLocaleConfig?): String? {
        // raw localisation text
        return doGetHintLocalisation(element, locale)?.value?.orNull()
    }

    override fun doGetHintLocalisation(element: ParadoxComplexEnumValueElement, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
        val name = element.name
        val localeToUse = locale ?: ParadoxLocaleManager.getPreferredLocaleConfig()
        return ParadoxComplexEnumValueManager.getNameLocalisation(name, element, localeToUse)
    }
}

class ParadoxInferredDynamicValueHintTextProvider : ParadoxHintTextProviderBase.DynamicValue() {
    override val source: Source get() = Source.NameLocalisation

    override fun doGetHintText(element: ParadoxDynamicValueElement, locale: CwtLocaleConfig?): String? {
        // raw localisation text
        return doGetHintLocalisation(element, locale)?.value?.orNull()
    }

    override fun doGetHintLocalisation(element: ParadoxDynamicValueElement, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
        val name = element.name
        val localeToUse = locale ?: ParadoxLocaleManager.getPreferredLocaleConfig()
        return ParadoxDynamicValueManager.getNameLocalisation(name, element, localeToUse)
    }
}

class ParadoxExtendedScriptedVariableHintTextProvider : ParadoxHintTextProviderBase.ScriptedVariable() {
    override val source get() = Source.Extended

    override fun doGetHintText(element: ParadoxScriptScriptedVariable, name: String, locale: CwtLocaleConfig?): String? {
        val gameType = selectGameType(element) ?: return null
        val project = element.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val config = configGroup.extendedScriptedVariables.findFromPattern(name, element, configGroup) ?: return null
        val hint = config.hint?.orNull()
        return hint
    }

    override fun doGetHintLocalisation(element: ParadoxScriptScriptedVariable, name: String, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
        val hint = doGetHintText(element, name, locale)?.orNull() ?: return null
        return createHintLocalisation(hint, element)
    }
}

class ParadoxExtendedDefinitionHintTextProvider : ParadoxHintTextProviderBase.Definition() {
    override val source get() = Source.Extended

    override fun doGetHintText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, locale: CwtLocaleConfig?): String? {
        val name = definitionInfo.name
        val configGroup = definitionInfo.configGroup
        val configs = configGroup.extendedDefinitions.findFromPattern(name, element, configGroup).orEmpty()
        val config = configs.findLast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) } ?: return null
        val hint = config.hint?.orNull()
        return hint
    }

    override fun doGetHintLocalisation(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
        val hint = doGetHintText(element, definitionInfo, locale)?.orNull() ?: return null
        return createHintLocalisation(hint, element)
    }
}

class ParadoxExtendedGameRuleHintTextProvider : ParadoxHintTextProviderBase.Definition() {
    override val source get() = Source.Extended

    override fun doGetHintText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, locale: CwtLocaleConfig?): String? {
        if (definitionInfo.type != ParadoxDefinitionTypes.GameRule) return null
        val name = definitionInfo.name
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedGameRules.findFromPattern(name, element, configGroup) ?: return null
        val hint = config.hint?.orNull()
        return hint
    }

    override fun doGetHintLocalisation(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
        val hint = doGetHintText(element, definitionInfo, locale)?.orNull() ?: return null
        return createHintLocalisation(hint, element)
    }
}

class ParadoxExtendedOnActionHintTextProvider : ParadoxHintTextProviderBase.Definition() {
    override val source get() = Source.Extended

    override fun doGetHintText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, locale: CwtLocaleConfig?): String? {
        if (definitionInfo.type != ParadoxDefinitionTypes.OnAction) return null
        val name = definitionInfo.name
        val configGroup = definitionInfo.configGroup
        val config = configGroup.extendedOnActions.findFromPattern(name, element, configGroup) ?: return null
        val hint = config.hint?.orNull()
        return hint
    }

    override fun doGetHintLocalisation(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
        val hint = doGetHintText(element, definitionInfo, locale)?.orNull() ?: return null
        return createHintLocalisation(hint, element)
    }
}

class ParadoxExtendedComplexEnumValueHintTextProvider : ParadoxHintTextProviderBase.ComplexEnumValue() {
    override val source get() = Source.Extended

    override fun doGetHintText(element: ParadoxComplexEnumValueElement, locale: CwtLocaleConfig?): String? {
        val name = element.name
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        val configs = configGroup.extendedComplexEnumValues[element.enumName] ?: return null
        val config = configs.findFromPattern(name, element, configGroup) ?: return null
        val hint = config.hint?.orNull()
        return hint
    }

    override fun doGetHintLocalisation(element: ParadoxComplexEnumValueElement, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
        val hint = doGetHintText(element, locale)?.orNull() ?: return null
        return createHintLocalisation(hint, element)
    }
}

class ParadoxExtendedDynamicValueHintTextProvider : ParadoxHintTextProviderBase.DynamicValue() {
    override val source get() = Source.Extended

    override fun doGetHintText(element: ParadoxDynamicValueElement, locale: CwtLocaleConfig?): String? {
        val name = element.name
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        for (type in element.dynamicValueTypes) {
            val configs = configGroup.extendedDynamicValues[type] ?: continue
            val config = configs.findFromPattern(name, element, configGroup) ?: continue
            val hint = config.hint?.orNull()
            if (hint != null) return hint
        }
        return null
    }

    override fun doGetHintLocalisation(element: ParadoxDynamicValueElement, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
        val hint = doGetHintText(element, locale)?.orNull() ?: return null
        return createHintLocalisation(hint, element)
    }
}
