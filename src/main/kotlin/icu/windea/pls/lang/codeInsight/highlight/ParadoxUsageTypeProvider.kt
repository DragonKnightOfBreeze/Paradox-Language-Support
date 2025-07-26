package icu.windea.pls.lang.codeInsight.highlight

import com.intellij.psi.*
import com.intellij.usages.*
import com.intellij.usages.impl.rules.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

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
            is ParadoxScriptExpressionElement -> getUsageType(element, targets)
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
            else -> null
        }
    }

    private fun getUsageType(element: ParadoxScriptExpressionElement, targets: Array<out UsageTarget>): UsageType? {
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

}
