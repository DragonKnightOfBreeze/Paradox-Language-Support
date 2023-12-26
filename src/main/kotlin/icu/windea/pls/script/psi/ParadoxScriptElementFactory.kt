package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.script.*
import icu.windea.pls.util.*

object ParadoxScriptElementFactory {
	@JvmStatic
	fun createDummyFile(project: Project, text: String): ParadoxScriptFile {
		return PsiFileFactory.getInstance(project).createFileFromText(ParadoxScriptLanguage, text).cast()
	}
	
	@JvmStatic
	fun createLine(project: Project):PsiElement{
		return createDummyFile(project, "\n").firstChild
	}
	
	fun createRootBlock(project: Project, text: String): ParadoxScriptRootBlock {
		return createDummyFile(project, text).findChild()!!
	}
	
	@JvmStatic
	fun createScriptedVariable(project: Project, text: String): ParadoxScriptScriptedVariable {
		return createRootBlock(project, text).findChild()!!
	}
	
	@JvmStatic
	fun createScriptedVariable(project: Project, name: String, value: String): ParadoxScriptScriptedVariable {
		return createRootBlock(project, "@$name = $value").findChild()!!
	}
	
	@JvmStatic
	fun createScriptedVariableName(project: Project, name: String): ParadoxScriptScriptedVariableName {
		return createScriptedVariable(project, name, "0").findChild()!!
	}
	
	@JvmStatic
	fun createProperty(project: Project, text: String): ParadoxScriptProperty {
		return createRootBlock(project, text).findChild()!!
	}
	
	@JvmStatic
	fun createProperty(project: Project, key: String, value: String): ParadoxScriptProperty {
		val newKey = buildString { ParadoxEscapeManager.escapeScriptExpression(key, this) }.quoteIfNecessary()
		val newValue = buildString { ParadoxEscapeManager.escapeScriptExpression(value, this) }.quoteIfNecessary()
		return createRootBlock(project, "$newKey = $newValue").findChild()!!
	}
	
	@JvmStatic
	fun createPropertyKey(project: Project, key: String): ParadoxScriptPropertyKey {
		return createProperty(project, key, "0").findChild()!!
	}
	
	@JvmStatic
	fun createValue(project: Project, value: String): ParadoxScriptValue {
		return createProperty(project, "a", value).findChild()!!
	}
	
	@JvmStatic
	fun createBlock(project: Project, value: String): ParadoxScriptBlock {
		return createValue(project, value).cast()!!
	}
	
	@JvmStatic
	fun createVariableReference(project: Project, name: String): ParadoxScriptScriptedVariableReference {
		return createValue(project, "@$name").cast()
	}
	
	@JvmStatic
	fun createString(project: Project, value: String): ParadoxScriptString {
		return createValue(project, value).cast()!!
	}
	
	@JvmStatic
	fun createParameterCondition(project: Project, expression: String, itemsText: String): ParadoxScriptParameterCondition {
		val text = "a = { [[$expression] $itemsText ] }"
		return createRootBlock(project, text)
			.findChild<ParadoxScriptProperty>()!!
			.findChild<ParadoxScriptBlock>()!!
			.findChild()!!
	}
	
	@JvmStatic
	fun createParameterConditionParameter(project: Project, name: String): ParadoxScriptParameterConditionParameter {
		return createParameterCondition(project, name, "a")
			.findChild<ParadoxScriptParameterConditionExpression>()!!
			.findChild()!!
	}
	
	@JvmStatic
	fun createInlineMath(project: Project, expression: String): ParadoxScriptInlineMath {
		return createValue(project, "@[$expression]").cast()
	}
	
	@JvmStatic
	fun createInlineMathVariableReference(project: Project, name: String): ParadoxScriptInlineMathScriptedVariableReference {
		return createInlineMath(project, name).findChild()!!
	}
	
	@JvmStatic
	fun createParameter(project: Project, name: String): ParadoxScriptParameter {
		return createValue(project, "\$$name\$").findChild()!!
	}
	
	@JvmStatic
	fun createInlineMathParameter(project: Project, name: String): ParadoxScriptInlineMathParameter {
		val text = "$$name$"
		return createInlineMath(project, text).findChild()!!
	}
}
