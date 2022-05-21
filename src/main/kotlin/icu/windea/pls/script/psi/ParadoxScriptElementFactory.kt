package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

object ParadoxScriptElementFactory {
	@JvmStatic
	fun createDummyFile(project: Project, text: String): ParadoxScriptFile {
		return PsiFileFactory.getInstance(project).createFileFromText(ParadoxScriptLanguage, text).cast()
	}
	
	fun createRootBlock(project: Project, text: String): ParadoxScriptRootBlock {
		return createDummyFile(project, text).findRequiredChild(ROOT_BLOCK)
	}
	
	@JvmStatic
	fun createVariable(project: Project, name: String, value: String): ParadoxScriptVariable {
		val usedName = if(name.startsWith('@')) name else "@$name"
		return createRootBlock(project, "$usedName=$value").findRequiredChild(VARIABLE)
	}
	
	@JvmStatic
	fun createVariableName(project: Project, name: String): ParadoxScriptVariableName {
		val usedName = if(name.startsWith('@')) name else "@$name"
		return createVariable(project, usedName, "0").findRequiredChild(VARIABLE_NAME)
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
	fun createVariableReference(project: Project, name: String): ParadoxScriptVariableReference{
		val usedName = if(name.startsWith('@')) name else "@$name"
		return createPropertyValue(project, usedName).findRequiredChild(VARIABLE_REFERENCE)
	}
	
	@JvmStatic
	fun createString(project: Project, value: String): ParadoxScriptString {
		val usedValue = value.quoteIfNecessary()
		return createRootBlock(project, usedValue).findRequiredChild()
	}
	
	@JvmStatic
	fun createInlineMath(project: Project, expression: String): ParadoxScriptInlineMath {
		return createPropertyValue(project, "@\\[$expression]").findRequiredChild(INLINE_MATH)
	}
	
	@JvmStatic
	fun createInlineMathVariableReference(project: Project, name: String): ParadoxScriptInlineMathVariableReference {
		return createInlineMath(project, name).findRequiredChild(INLINE_MATH_VARIABLE_REFERENCE)
	}
	
	@JvmStatic
	fun createInlineMathParameter(project: Project, name: String): ParadoxScriptInlineMathParameter {
		val usedName = "$$name$"
		return createInlineMath(project, usedName).findRequiredChild(INLINE_MATH_PARAMETER)
	}
}
