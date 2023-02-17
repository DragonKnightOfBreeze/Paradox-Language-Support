package icu.windea.pls.core.codeInsight.template.postfix

import com.intellij.codeInsight.template.postfix.templates.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.setting.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxVariableOperationExpressionPostfixTemplate(
	setting: CwtPostfixTemplateSetting,
	provider: PostfixTemplateProvider
): ParadoxExpressionEditablePostfixTemplate(setting, provider) {
	companion object {
		const val GROUP_NAME = "variable_operation_expressions"
		
	}
	
	override val groupName: String get() = GROUP_NAME
	
	override fun getExpressions(context: PsiElement, document: Document, offset: Int): List<PsiElement> {
		if(!ParadoxScriptTokenSets.VARIABLE_VALUES.contains(context.elementType)) return emptyList()
		ProgressManager.checkCanceled()
		val stringElement = context.parent?.castOrNull<ParadoxScriptValue>() ?: return emptyList()
		if(!stringElement.isBlockValue()) return emptyList()
		val parentProperty = stringElement.findParentProperty() ?: return emptyList()
		val expression = ParadoxDataExpression.resolve(setting.id, false, true)
		val configs = ParadoxCwtConfigHandler.getConfigs(parentProperty, allowDefinition = true)
		if(configs.isEmpty()) return emptyList()
		val configGroup = configs.first().info.configGroup
		val matched = configs.any { config ->
			config.configs?.any { childConfig ->
				childConfig is CwtPropertyConfig && CwtConfigHandler.matchesScriptExpression(context, expression, childConfig.keyExpression, childConfig, configGroup)
			} ?: false
		}
		if(!matched) return emptyList()
		return stringElement.toSingletonList()
	}
}