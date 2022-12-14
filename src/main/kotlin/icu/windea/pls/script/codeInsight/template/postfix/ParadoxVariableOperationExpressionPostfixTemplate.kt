package icu.windea.pls.script.codeInsight.template.postfix

import com.intellij.codeInsight.template.postfix.templates.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.setting.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.handler.*
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
		if(!ParadoxScriptTokenSets.variableValueTokens.contains(context.elementType)) return emptyList()
		ProgressManager.checkCanceled()
		val stringElement = context.parent?.castOrNull<ParadoxScriptValue>() ?: return emptyList()
		if(!stringElement.isBlockValue()) return emptyList()
		val parentProperty = stringElement.findParentDefinitionProperty() ?: return emptyList()
		val definitionElementInfo = ParadoxDefinitionElementHandler.getInfo(parentProperty) ?: return emptyList()
		val configGroup = definitionElementInfo.configGroup
		val childPropertyConfigs = definitionElementInfo.getChildPropertyConfigs()
		val expression = ParadoxDataExpression.resolve(setting.id, false, true)
		val config = childPropertyConfigs.find { CwtConfigHandler.matchesScriptExpression(expression, it.keyExpression, configGroup) }
		if(config == null) return emptyList()
		return stringElement.toSingletonList()
	}
}