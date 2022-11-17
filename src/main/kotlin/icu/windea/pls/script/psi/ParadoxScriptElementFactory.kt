package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

object ParadoxScriptElementFactory {
	@JvmStatic
	fun createDummyFile(project: Project, text: String): ParadoxScriptFile {
		return PsiFileFactory.getInstance(project).createFileFromText(ParadoxScriptLanguage, text).cast()
	}
	
	@JvmStatic
	fun createLine(project: Project):PsiElement{
		return ParadoxLocalisationElementFactory.createDummyFile(project, "\n").firstChild
	}
	
	fun createRootBlock(project: Project, text: String): ParadoxScriptRootBlock {
		return createDummyFile(project, text).findRequiredChild(ROOT_BLOCK)
	}
	
	@JvmStatic
	fun createVariable(project: Project, name: String, value: String): ParadoxScriptScriptedVariable {
		return createRootBlock(project, "@$name=$value").findRequiredChild(SCRIPTED_VARIABLE)
	}
	
	@JvmStatic
	fun createVariableName(project: Project, name: String): ParadoxScriptScriptedVariableName {
		return createVariable(project, name, "0").findRequiredChild(SCRIPTED_VARIABLE_NAME)
	}
	
	@JvmStatic
	fun createProperty(project: Project, key: String, value: String): ParadoxScriptProperty {
		val usedKey = key.quoteIfNecessary()
		return createRootBlock(project, "$usedKey=$value").findRequiredChild(PROPERTY)
	}
	
	@JvmStatic
	fun createPropertyKey(project: Project, key: String): ParadoxScriptPropertyKey {
		return createProperty(project, key, "0").findRequiredChild(PROPERTY_KEY)
	}
	
	@JvmStatic
	fun createPropertyValue(project: Project, value: String): ParadoxScriptPropertyValue {
		return createProperty(project, "a", value).findRequiredChild(PROPERTY_VALUE)
	}
	
	@JvmStatic
	fun createValue(project: Project, value: String): ParadoxScriptValue {
		return createRootBlock(project, value).findRequiredChild()
	}
	
	@JvmStatic
	fun createVariableReference(project: Project, name: String): ParadoxScriptScriptedVariableReference {
		return createPropertyValue(project, "@$name").findRequiredChild(SCRIPTED_VARIABLE_REFERENCE)
	}
	
	@JvmStatic
	fun createString(project: Project, value: String): ParadoxScriptString {
		val usedValue = value.quoteIfNecessary()
		return createRootBlock(project, usedValue).findRequiredChild()
	}
	
	@JvmStatic
	fun createInlineMath(project: Project, expression: String): ParadoxScriptInlineMath {
		return createPropertyValue(project, "@[$expression]").findRequiredChild(INLINE_MATH)
	}
	
	@JvmStatic
	fun createInlineMathVariableReference(project: Project, name: String): ParadoxScriptInlineMathScriptedVariableReference {
		return createInlineMath(project, name).findRequiredChild(INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE)
	}
	
	@JvmStatic
	fun createParameter(project: Project, name: String): ParadoxScriptParameter {
		val text = "$$name$"
		return createRootBlock(project, text).findRequiredChild<ParadoxScriptString>().findRequiredChild()
	}
	
	@JvmStatic
	fun createParameterConditionParameter(project: Project, name: String): ParadoxScriptParameterConditionParameter {
		val text = "a = { [[$name] = a }"
		return createRootBlock(project, text)
			.findRequiredChild<ParadoxScriptProperty>()
			.findRequiredChild<ParadoxScriptPropertyValue>()
			.findRequiredChild<ParadoxScriptBlock>()
			.findRequiredChild<ParadoxScriptParameterCondition>()
			.findRequiredChild<ParadoxScriptParameterConditionExpression>()
			.findRequiredChild()
	}
	
	@JvmStatic
	fun createInlineMathParameter(project: Project, name: String): ParadoxScriptInlineMathParameter {
		val text = "$$name$"
		return createInlineMath(project, text).findRequiredChild(INLINE_MATH_PARAMETER)
	}
}
