package icu.windea.pls.lang.codeInsight.highlight

import com.intellij.psi.PsiElement
import com.intellij.usages.PsiElementUsageTarget
import com.intellij.usages.UsageTarget
import com.intellij.usages.impl.rules.UsageType
import com.intellij.usages.impl.rules.UsageTypeProviderEx
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.isHeaderColumn
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.util.ParadoxCsvManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommandText
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptName
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationScriptedVariableReference
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptInlineMathParameter
import icu.windea.pls.script.psi.ParadoxScriptInlineMathScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptParameter
import icu.windea.pls.script.psi.ParadoxScriptParameterConditionParameter
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isResolvableExpression

/**
 * 在查找使用中，区分定义、本地化等各种声明的使用类型。
 */
class ParadoxUsageTypeProvider : UsageTypeProviderEx {
    override fun getUsageType(element: PsiElement): UsageType? {
        return getUsageType(element, UsageTarget.EMPTY_ARRAY)
    }

    override fun getUsageType(element: PsiElement, targets: Array<out UsageTarget>): UsageType? {
        if (element.language !is ParadoxBaseLanguage) return null
        return when (element) {
            is ParadoxScriptScriptedVariableReference -> ParadoxUsageTypes.SCRIPTED_VARIABLE_REFERENCE_1
            is ParadoxScriptInlineMathScriptedVariableReference -> ParadoxUsageTypes.SCRIPTED_VARIABLE_REFERENCE_2
            is ParadoxLocalisationScriptedVariableReference -> ParadoxUsageTypes.SCRIPTED_VARIABLE_REFERENCE_3
            is ParadoxScriptExpressionElement -> doGetUsageType(element, targets)
            is ParadoxScriptParameter -> ParadoxUsageTypes.PARAMETER_REFERENCE_1
            is ParadoxScriptInlineMathParameter -> ParadoxUsageTypes.PARAMETER_REFERENCE_2
            is ParadoxScriptParameterConditionParameter -> ParadoxUsageTypes.PARAMETER_REFERENCE_3
            is ParadoxLocalisationParameter -> ParadoxUsageTypes.LOCALISATION_REFERENCE
            is ParadoxLocalisationIcon -> ParadoxUsageTypes.LOCALISATION_ICON
            is ParadoxLocalisationColorfulText -> ParadoxUsageTypes.LOCALISATION_COLOR
            is ParadoxLocalisationCommandText -> ParadoxUsageTypes.LOCALISATION_COMMAND_TEXT
            is ParadoxLocalisationConceptName -> ParadoxUsageTypes.LOCALISATION_CONCEPT_NAME
            is ParadoxLocalisationTextIcon -> ParadoxUsageTypes.LOCALISATION_TEXT_ICON
            is ParadoxLocalisationTextFormat -> ParadoxUsageTypes.LOCALISATION_TEXT_FORMAT
            is ParadoxCsvExpressionElement -> doGetUsageType(element)
            else -> null
        }
    }

    private fun doGetUsageType(element: ParadoxScriptExpressionElement, targets: Array<out UsageTarget>): UsageType? {
        //#131
        if (!element.isResolvableExpression()) return null

        //尝试解析为复杂枚举值声明
        run {
            if (element !is ParadoxScriptStringExpressionElement) return@run
            val resolvedElements = targets.mapNotNull { it.castOrNull<PsiElementUsageTarget>()?.element }
            val resolved = resolvedElements.findIsInstance<ParadoxComplexEnumValueElement>()
            if (resolved == null) return@run
            return ParadoxUsageTypes.COMPLEX_ENUM_VALUE
        }

        val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return null
        val configExpression = config.configExpression
        val type = configExpression.type
        //in invocation expression
        if (config.configExpression.type == CwtDataTypes.Parameter) {
            return ParadoxUsageTypes.PARAMETER_REFERENCE_4
        }
        //in script value expression
        if (type in CwtDataTypeGroups.ValueField) {
            val targetElement = targets.firstOrNull()?.castOrNull<PsiElementUsageTarget>()?.element
            if (targetElement is ParadoxParameterElement) {
                return ParadoxUsageTypes.PARAMETER_REFERENCE_5
            }
        }
        //in invocation expression (for localisation parameters)
        if (config.configExpression.type == CwtDataTypes.LocalisationParameter) {
            return ParadoxUsageTypes.PARAMETER_REFERENCE_6
        }
        return ParadoxUsageTypes.FROM_CONFIG_EXPRESSION(configExpression)
    }

    private fun doGetUsageType(element: ParadoxCsvExpressionElement): UsageType? {
        if (element !is ParadoxCsvColumn) return null

        if (element.isHeaderColumn()) {
            return ParadoxUsageTypes.HEADER_COLUMN
        }

        val columnConfig = ParadoxCsvManager.getColumnConfig(element) ?: return null
        val config = columnConfig.valueConfig ?: return null
        val configExpression = config.configExpression
        return ParadoxUsageTypes.FROM_CONFIG_EXPRESSION(configExpression)
    }
}
