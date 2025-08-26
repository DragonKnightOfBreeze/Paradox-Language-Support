package icu.windea.pls.ep.codeInsight.hints

import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

class ParadoxInferredScriptedVariableHintTextProvider : ParadoxHintTextProviderBase.ScriptedVariable() {
    override val source: Source get() = Source.NameLocalisation

    override fun doGetHintText(element: ParadoxScriptScriptedVariable, name: String, locale: CwtLocaleConfig?): String? {
        // use raw localisation text here
        return doGetHintLocalisation(element, name, locale)?.value?.orNull()
    }

    override fun doGetHintLocalisation(element: ParadoxScriptScriptedVariable, name: String, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
        val localeToUse = locale ?: ParadoxLocaleManager.getPreferredLocaleConfig()
        val selector = selector(element.project, element).localisation().contextSensitive().preferLocale(localeToUse)
        return ParadoxLocalisationSearch.search(name, selector).find()
    }
}

class ParadoxInferredComplexEnumValueHintTextProvider : ParadoxHintTextProviderBase.ComplexEnumValue() {
    override val source: Source get() = Source.NameLocalisation

    override fun doGetHintText(element: ParadoxComplexEnumValueElement, locale: CwtLocaleConfig?): String? {
        // use raw localisation text here
        return doGetHintLocalisation(element, locale)?.value?.orNull()
    }

    override fun doGetHintLocalisation(element: ParadoxComplexEnumValueElement, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
        val name = element.name
        val localeToUse = locale ?: ParadoxLocaleManager.getPreferredLocaleConfig()
        val selector = selector(element.project, element).localisation().contextSensitive().preferLocale(localeToUse)
        return ParadoxLocalisationSearch.search(name, selector).find()
    }
}

class ParadoxInferredDynamicValueHintTextProvider : ParadoxHintTextProviderBase.DynamicValue() {
    override val source: Source get() = Source.NameLocalisation

    override fun doGetHintText(element: ParadoxDynamicValueElement, locale: CwtLocaleConfig?): String? {
        // use raw localisation text here
        return doGetHintLocalisation(element, locale)?.value?.orNull()
    }

    override fun doGetHintLocalisation(element: ParadoxDynamicValueElement, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
        val name = element.name
        val localeToUse = locale ?: ParadoxLocaleManager.getPreferredLocaleConfig()
        val selector = selector(element.project, element).localisation().contextSensitive().preferLocale(localeToUse)
        return ParadoxLocalisationSearch.search(name, selector).find()
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
