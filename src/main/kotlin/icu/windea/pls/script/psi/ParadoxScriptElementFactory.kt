package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.*

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
	fun createScriptedVariableFromText(project: Project, text: String): ParadoxScriptScriptedVariable {
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
	fun createPropertyFromText(project: Project, text: String): ParadoxScriptProperty {
		return createRootBlock(project, text).findChild()!!
	}
	
	@JvmStatic
	fun createProperty(project: Project, key: String, value: String): ParadoxScriptProperty {
		val newKey = key.quoteIfNecessary(or = key.isQuoted())
		val newValue = value.quoteIfNecessary(or = value.isQuoted())
		return createRootBlock(project, "$newKey = $newValue").findChild()!!
	}
	
	@JvmStatic
	fun createPropertyKeyFromText(project: Project, text: String): ParadoxScriptPropertyKey {
		return createPropertyFromText(project, "$text = v").findChild()!!
	}
	
	@JvmStatic
	fun createPropertyKey(project: Project, key: String): ParadoxScriptPropertyKey {
		return createProperty(project, key, "0").findChild()!!
	}
	
	@JvmStatic
	fun createValueFromText(project: Project, text: String): ParadoxScriptValue {
		return createPropertyFromText(project, "k = $text").findChild()!!
	}
	
	@JvmStatic
	fun createValue(project: Project, value: String): ParadoxScriptValue {
		return createProperty(project, "a", value).findChild()!!
	}
	
	@JvmStatic
	fun createStringFromText(project: Project, text: String): ParadoxScriptString {
		return createValueFromText(project, text).castOrNull<ParadoxScriptString>()
			?: createValueFromText(project, text.quote()).cast()
	}
	
	@JvmStatic
	fun createString(project: Project, value: String): ParadoxScriptString {
		return createValue(project, value).castOrNull<ParadoxScriptString>()
			?: createValue(project, value.quote()).cast()
	}
	
	@JvmStatic
	fun createVariableReference(project: Project, name: String): ParadoxScriptScriptedVariableReference {
		return createValue(project, "@$name").cast()
	}
	
	@JvmStatic
	fun createBlock(project: Project, value: String): ParadoxScriptBlock {
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
	fun createParameterFromText(project: Project, text: String): ParadoxScriptParameter {
		return createValueFromText(project, text).findChild()!!
	}
	
	@JvmStatic
	fun createParameter(project: Project, name: String, defaultValue: String? = null): ParadoxScriptParameter {
		val text = if(defaultValue == null) "$$name$" else "$$name|$defaultValue$"
		return createValue(project, text).findChild()!!
	}
	
	@JvmStatic
	fun createInlineMathParameterFromText(project: Project, text: String): ParadoxScriptParameter {
		return createInlineMath(project, text).findChild()!!
	}
	
	@JvmStatic
	fun createInlineMathParameter(project: Project, name: String, defaultValue: String? = null): ParadoxScriptInlineMathParameter {
		val text = if(defaultValue == null) "$$name$" else "$$name|$defaultValue$"
		return createInlineMath(project, text).findChild()!!
	}
}
