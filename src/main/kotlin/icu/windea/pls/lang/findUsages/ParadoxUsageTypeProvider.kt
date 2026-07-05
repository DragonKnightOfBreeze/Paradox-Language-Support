package icu.windea.pls.lang.findUsages

import com.intellij.psi.PsiElement
import com.intellij.usages.PsiElementUsageTarget
import com.intellij.usages.UsageTarget
import com.intellij.usages.impl.rules.UsageType
import com.intellij.usages.impl.rules.UsageTypeProviderEx
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.ReadWriteAccess
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.lang.ParadoxLanguage
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName
import icu.windea.pls.lang.psi.isResolvableLiteralExpression
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxCsvManager
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommandText
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptName
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationScriptedVariableReference
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptConditionalBlockParameter
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptInlineMathParameter
import icu.windea.pls.script.psi.ParadoxScriptInlineMathScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptParameter
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 在查找用法中，区分定义、本地化等各种声明的用法类型。
 */
class ParadoxUsageTypeProvider : UsageTypeProviderEx {
    override fun getUsageType(element: PsiElement): UsageType? {
        return getUsageType(element, UsageTarget.EMPTY_ARRAY)
    }

    override fun getUsageType(element: PsiElement, targets: Array<out UsageTarget>): UsageType? {
        if (element.language !is ParadoxLanguage) return null
        return when (element) {
            is ParadoxScriptScriptedVariableReference -> ParadoxUsageTypes.SCRIPTED_VARIABLE_REFERENCE
            is ParadoxScriptInlineMathScriptedVariableReference -> ParadoxUsageTypes.SCRIPTED_VARIABLE_REFERENCE_1
            is ParadoxLocalisationScriptedVariableReference -> ParadoxUsageTypes.SCRIPTED_VARIABLE_REFERENCE_2
            is ParadoxScriptExpressionElement -> doGetUsageType(element, targets)
            is ParadoxScriptParameter -> ParadoxUsageTypes.PARAMETER_REFERENCE
            is ParadoxScriptInlineMathParameter -> ParadoxUsageTypes.PARAMETER_REFERENCE_1
            is ParadoxScriptConditionalBlockParameter -> ParadoxUsageTypes.PARAMETER_REFERENCE_2
            is ParadoxLocalisationParameter -> ParadoxUsageTypes.LOCALISATION_PARAMETER
            is ParadoxLocalisationColorfulText -> ParadoxUsageTypes.LOCALISATION_COLOR
            is ParadoxLocalisationIcon -> ParadoxUsageTypes.LOCALISATION_ICON
            is ParadoxLocalisationCommandText -> ParadoxUsageTypes.LOCALISATION_COMMAND_TEXT
            is ParadoxLocalisationConceptName -> ParadoxUsageTypes.LOCALISATION_CONCEPT_NAME
            is ParadoxLocalisationTextIcon -> ParadoxUsageTypes.LOCALISATION_TEXT_ICON
            is ParadoxLocalisationTextFormat -> ParadoxUsageTypes.LOCALISATION_TEXT_FORMAT
            is ParadoxCsvExpressionElement -> doGetUsageType(element)
            else -> null
        }
    }

    private fun doGetUsageType(element: ParadoxScriptExpressionElement, targets: Array<out UsageTarget>): UsageType? {
        // #131
        if (!element.isResolvableLiteralExpression()) return null

        for (target in targets) {
            if (target is PsiElementUsageTarget) {
                val targetElement = target.element
                run {
                    // complex enum value declaration
                    if (targetElement !is ParadoxComplexEnumValueLightElement) return@run
                    if (targetElement.readWriteAccess != ReadWriteAccess.Write) return@run
                    return ParadoxUsageTypes.COMPLEX_ENUM_VALUE_DECLARATION
                }
                run {
                    // event name space reference (in event id declaration)
                    if (targetElement !is ParadoxScriptProperty) return@run
                    if (element !is ParadoxScriptStringExpressionElement) return@run
                    if (!element.isDefinitionTypeKeyOrName()) return@run
                    if (selectScope { element.parentDefinition().asDefinition(ParadoxDefinitionTypes.event) } == null) return@run
                    if (selectScope { targetElement.asDefinition(ParadoxDefinitionTypes.eventNamespace) } == null) return@run
                    return ParadoxUsageTypes.EVENT_NAMESPACE_REFERENCE
                }
                run {
                    // definition injection target
                    if (targetElement !is ParadoxScriptProperty) return@run
                    if (element !is ParadoxScriptPropertyKey) return@run
                    if (!ParadoxDefinitionInjectionManager.isMatched(element.name, element)) return@run
                    if (targetElement.definitionInfo == null) return@run
                    return ParadoxUsageTypes.DEFINITION_INJECTION_TARGET
                }
            }
        }

        val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return null
        val configExpression = config.configExpression
        // in call expression
        if (configExpression.type == CwtDataTypes.Parameter) {
            return ParadoxUsageTypes.PARAMETER_REFERENCE_3
        }
        // in script value reference expression
        if (configExpression.type in CwtDataTypeSets.ValueField) {
            if (targets.any { it is PsiElementUsageTarget && it.element is ParadoxParameterLightElement }) {
                return ParadoxUsageTypes.PARAMETER_REFERENCE_4
            }
        }
        // in call expression (for localisation parameters)
        if (configExpression.type == CwtDataTypes.LocalisationParameter) {
            return ParadoxUsageTypes.LOCALISATION_PARAMETER_REFERENCE
        }
        // from config expression
        return ParadoxUsageTypes.FROM_CONFIG_EXPRESSION(configExpression)
    }

    private fun doGetUsageType(element: ParadoxCsvExpressionElement): UsageType? {
        if (element !is ParadoxCsvColumn) return null

        if (ParadoxCsvPsiService.isHeaderColumn(element)) {
            return ParadoxUsageTypes.HEADER_COLUMN
        }

        val columnConfig = ParadoxCsvManager.getColumnConfig(element) ?: return null
        val config = columnConfig.valueConfig ?: return null
        val configExpression = config.configExpression
        // from config expression
        return ParadoxUsageTypes.FROM_CONFIG_EXPRESSION(configExpression)
    }
}
