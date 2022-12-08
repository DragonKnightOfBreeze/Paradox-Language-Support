package icu.windea.pls.core.codeInsight.highlight

import com.intellij.psi.*
import com.intellij.usages.*
import com.intellij.usages.impl.rules.*
import icu.windea.pls.core.handler.ParadoxCwtConfigHandler.resolveConfigs
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 在查找使用中，区分定义、本地化、参数、值集值值等的使用类型。
 */
class ParadoxUsageTypeProvider : UsageTypeProviderEx {
	override fun getUsageType(element: PsiElement): UsageType? {
		return getUsageType(element, UsageTarget.EMPTY_ARRAY)
	}
	
	override fun getUsageType(element: PsiElement?, targets: Array<out UsageTarget>): UsageType? {
		//TODO
		return when {
			element is ParadoxScriptStringExpressionElement -> {
				val config = resolveConfigs(element).firstOrNull() ?: return null
				val configExpression = config.expression
				ParadoxUsageType.FROM_CONFIG_EXPRESSION(configExpression)
			}
			element is ParadoxScriptScriptedVariableReference -> ParadoxUsageType.SCRIPTED_VARIABLE_REFERENCE_1
			element is ParadoxScriptInlineMathScriptedVariableReference -> ParadoxUsageType.SCRIPTED_VARIABLE_REFERENCE_1
			element is ParadoxScriptParameter -> ParadoxUsageType.PARAMETER_REFERENCE_1
			element is ParadoxScriptInlineMathParameter -> ParadoxUsageType.PARAMETER_REFERENCE_2
			element is ParadoxScriptParameterConditionParameter -> ParadoxUsageType.PARAMETER_REFERENCE_3
			element is ParadoxLocalisationPropertyReference -> ParadoxUsageType.LOCALISATION_REFERENCE
			else -> null
		}
	}
}
