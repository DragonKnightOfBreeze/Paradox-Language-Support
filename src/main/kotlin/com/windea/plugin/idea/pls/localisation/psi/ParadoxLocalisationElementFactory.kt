package com.windea.plugin.idea.pls.localisation.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.windea.plugin.idea.pls.localisation.*

object ParadoxLocalisationElementFactory {
	@JvmStatic
	fun createDummyFile(project: Project, text: String): ParadoxLocalisationFile {
		return PsiFileFactory.getInstance(project).createFileFromText(ParadoxLocalisationLanguage, text) as ParadoxLocalisationFile
	}

	@JvmStatic
	fun createLocale(project: Project, locale: String): ParadoxLocalisationLocale {
		return createDummyFile(project, "$locale:").locale!!
	}

	@JvmStatic
	fun createProperty(project: Project, key: String, value: String): ParadoxLocalisationProperty {
		return createDummyFile(project, "l_english:\n$key:0 \"$value\"").properties.first()
	}

	@JvmStatic
	fun createPropertyKey(project: Project, key: String): ParadoxLocalisationPropertyKey {
		return createProperty(project, key, "").propertyKey
	}

	@JvmStatic
	fun createPropertyValue(project: Project, value: String): ParadoxLocalisationPropertyValue {
		return createProperty(project, "a", value).propertyValue!!
	}

	@JvmStatic
	fun createPropertyReference(project: Project, name: String): ParadoxLocalisationPropertyReference {
		return createPropertyValue(project, "$$name$").richTextList.first() as ParadoxLocalisationPropertyReference
	}

	@JvmStatic
	fun createIcon(project: Project, name: String): ParadoxLocalisationIcon {
		return createPropertyValue(project, "£$name£").richTextList.first() as ParadoxLocalisationIcon
	}

	@JvmStatic
	fun createSequentialNumber(project: Project, name: String): ParadoxLocalisationSequentialNumber {
		return createPropertyValue(project, "%$name%").richTextList.first() as ParadoxLocalisationSequentialNumber
	}

	@JvmStatic
	fun createColorfulText(project: Project, name: String,value:String = ""): ParadoxLocalisationColorfulText {
		return createPropertyValue(project, "§$name$value§!").richTextList.first() as ParadoxLocalisationColorfulText
	}
	
	@JvmStatic
	fun createCommandScope(project: Project, name: String): ParadoxLocalisationCommandScope {
		val command = createPropertyValue(project, "[$name.GetName]").richTextList.first() as ParadoxLocalisationCommand
		return PsiTreeUtil.getChildOfType(command,ParadoxLocalisationCommandScope::class.java)!!
	}
	
	@JvmStatic
	fun createCommandField(project: Project, name: String): ParadoxLocalisationCommandField {
		val command = createPropertyValue(project, "[$name]").richTextList.first() as ParadoxLocalisationCommand
		return PsiTreeUtil.getChildOfType(command, ParadoxLocalisationCommandField::class.java)!!
	}
}
