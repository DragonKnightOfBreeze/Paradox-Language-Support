package icu.windea.pls.script.codeInsight.template

import com.intellij.codeInsight.template.postfix.templates.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.setting.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.script.psi.*

class ParadoxVariableOperationExpressionPostfixTemplate(
	setting: CwtPostfixTemplateSetting,
	provider: PostfixTemplateProvider
): ParadoxEditablePostfixTemplate(setting, provider) {
	companion object {
		val groupName = "variable_operation_expressions"
	}
	
	override fun getExpressions(context: PsiElement, document: Document, offset: Int): List<PsiElement> {
		//上下文属性必须是数字或字符串，并且直接在块中
		if(context !is ParadoxScriptInt && context !is ParadoxScriptFloat && context !is ParadoxScriptString) return emptyList()
		if(context !is ParadoxScriptValue) return emptyList()
		if(!context.isBlockValue()) return emptyList()
		val parentProperty = context.findParentDefinitionProperty() ?: return emptyList()
		val definitionElementInfo = ParadoxDefinitionElementInfoHandler.get(parentProperty) ?: return emptyList()
		val childPropertyConfigs = definitionElementInfo.getChildPropertyConfigs()
		val config = childPropertyConfigs.find { it.key == setting.name }
		if(config == null) return emptyList()
		return context.toSingletonList()
	}
}
