package icu.windea.pls.script.codeInsight.template

import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.impl.*
import com.intellij.codeInsight.template.postfix.templates.*
import com.intellij.codeInsight.template.postfix.templates.editable.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.setting.*

abstract class ParadoxEditablePostfixTemplate(
	val setting: CwtPostfixTemplateSetting,
	provider: PostfixTemplateProvider
): EditablePostfixTemplate(setting.name, setting.name, createTemplate(setting), setting.example.orEmpty(), provider) {
	override fun isBuiltin(): Boolean {
		return true
	}
	
	override fun addTemplateVariables(element: PsiElement, template: Template) {
		val variables = setting.variables
		if(variables.isEmpty()) return
		for(variable in variables) {
			template.addVariable(variable.key, "", variable.value, true)
		}
	}
	
	override fun equals(other: Any?): Boolean {
		return this === other || (other is ParadoxEditablePostfixTemplate && setting == other.setting) 
	}
	
	override fun hashCode(): Int {
		return setting.hashCode()
	}
}

private fun createTemplate(setting: CwtPostfixTemplateSetting): TemplateImpl {
	val template = TemplateImpl("fakeKey", setting.expression, "")
	template.isToReformat = true
	template.parseSegments()
	return template
}
