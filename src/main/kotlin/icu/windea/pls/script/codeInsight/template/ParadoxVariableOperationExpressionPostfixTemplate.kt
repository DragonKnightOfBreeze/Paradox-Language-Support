package icu.windea.pls.script.codeInsight.template

import cn.yiiguxing.plugin.translate.util.*
import com.intellij.codeInsight.template.postfix.templates.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.setting.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
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
		val contextType = context.elementType
		if(contextType != ParadoxScriptElementTypes.STRING_TOKEN) return emptyList()
		ProgressManager.checkCanceled()
		val stringElement = context.parent?.castOrNull<ParadoxScriptString>() ?: return emptyList()
		if(!stringElement.isBlockValue()) return emptyList()
		val parentProperty = stringElement.findParentDefinitionProperty() ?: return emptyList()
		val definitionElementInfo = ParadoxDefinitionElementInfoHandler.get(parentProperty) ?: return emptyList()
		val childPropertyConfigs = definitionElementInfo.getChildPropertyConfigs()
		val config = childPropertyConfigs.find { it.key == setting.id }
		if(config == null) return emptyList()
		return stringElement.toSingletonList()
	}
}
