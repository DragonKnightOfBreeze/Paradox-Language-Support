package com.windea.plugin.idea.paradox.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.script.*

object ParadoxScriptElementFactory {
	@JvmStatic
	fun createDummyFile(project: Project, text: String): ParadoxScriptFile {
		return PsiFileFactory.getInstance(project).createFileFromText(ParadoxScriptLanguage, text) as ParadoxScriptFile
	}

	@JvmStatic
	fun createVariable(project: Project, name: String, value: String): ParadoxScriptVariable {
		return createDummyFile(project, "$name=$value").variables.first()
	}

	@JvmStatic
	fun createVariableName(project: Project, name: String): ParadoxScriptVariableName {
		return createVariable(project, name, "0").variableName
	}

	@JvmStatic
	fun createVariableValue(project: Project, value: String): ParadoxScriptVariableValue {
		return createVariable(project, "a", value).variableValue!!
	}

	@JvmStatic
	fun createProperty(project: Project, key: String, value: String): ParadoxScriptProperty {
		return createDummyFile(project, "$key=$value").properties.first()
	}

	@JvmStatic
	fun createPropertyKey(project: Project, key: String): ParadoxScriptPropertyKey {
		return createProperty(project, key, "0").propertyKey
	}

	@JvmStatic
	fun createPropertyValue(project: Project, value: String): ParadoxScriptPropertyValue {
		return createProperty(project, "a", value).propertyValue!!
	}

	@JvmStatic
	fun createValue(project:Project,value:String):ParadoxScriptValue{
		return createPropertyValue(project,value).value
	}
}
