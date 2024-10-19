package icu.windea.pls.lang.codeInsight.highlight

import com.intellij.psi.*
import com.intellij.usages.*
import com.intellij.usages.impl.rules.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.psi.*
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
        when {
            element is ParadoxScriptStringExpressionElement || element is ParadoxScriptInt -> {
                //尝试解析为复杂枚举值声明
                val resolvedElements = targets.mapNotNull { it.castOrNull<PsiElementUsageTarget>()?.element }
                val complexEnumValueElement = resolvedElements.findIsInstance<ParadoxComplexEnumValueElement>()
                if (complexEnumValueElement != null) {
                    return ParadoxUsageType.COMPLEX_ENUM_VALUE
                }

                val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return null
                val configExpression = config.expression
                val type = configExpression.type
                //in invocation expression
                if (config.expression.type == CwtDataTypes.Parameter) {
                    return ParadoxUsageType.PARAMETER_REFERENCE_4
                }
                //in script value expression
                if (type in CwtDataTypeGroups.ValueField) {
                    val targetElement = getTargetElement(targets)
                    if (targetElement is ParadoxParameterElement) {
                        return ParadoxUsageType.PARAMETER_REFERENCE_5
                    }
                }
                //in invocation expression (for localisation parameters)
                if (config.expression.type == CwtDataTypes.LocalisationParameter) {
                    return ParadoxUsageType.PARAMETER_REFERENCE_6
                }
                return ParadoxUsageType.FROM_CONFIG_EXPRESSION(configExpression)
            }

            element is ParadoxScriptScriptedVariableReference -> return ParadoxUsageType.SCRIPTED_VARIABLE_REFERENCE_1
            element is ParadoxScriptInlineMathScriptedVariableReference -> return ParadoxUsageType.SCRIPTED_VARIABLE_REFERENCE_2
            element is ParadoxLocalisationScriptedVariableReference -> return ParadoxUsageType.SCRIPTED_VARIABLE_REFERENCE_3

            element is ParadoxScriptParameter -> return ParadoxUsageType.PARAMETER_REFERENCE_1
            element is ParadoxScriptInlineMathParameter -> return ParadoxUsageType.PARAMETER_REFERENCE_2
            element is ParadoxScriptParameterConditionParameter -> return ParadoxUsageType.PARAMETER_REFERENCE_3
            element is ParadoxLocalisationPropertyReference -> return ParadoxUsageType.LOCALISATION_REFERENCE
            element is ParadoxLocalisationIcon -> return ParadoxUsageType.LOCALISATION_ICON
            element is ParadoxLocalisationColorfulText -> return ParadoxUsageType.LOCALISATION_COLOR
            element is ParadoxLocalisationCommandText -> return ParadoxUsageType.LOCALISATION_COMMAND_TEXT
            element is ParadoxLocalisationConceptName -> return ParadoxUsageType.LOCALISATION_CONCEPT_NAME
            else -> return null
        }
    }

    private fun getTargetElement(targets: Array<out UsageTarget>): PsiElement? {
        return targets.firstOrNull()?.castOrNull<PsiElementUsageTarget>()?.element
    }
}
