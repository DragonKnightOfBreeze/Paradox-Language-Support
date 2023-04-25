package icu.windea.pls.localisation.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.*

object ParadoxLocalisationElementFactory {
	@JvmStatic
	fun createDummyFile(project: Project, text: String): ParadoxLocalisationFile {
		return PsiFileFactory.getInstance(project).createFileFromText(ParadoxLocalisationLanguage, text).cast()
	}
	
	@JvmStatic
	fun createLocale(project: Project, locale: String): ParadoxLocalisationLocale {
		return createDummyFile(project, "$locale:\n").findChild<ParadoxLocalisationPropertyList>()!!.findChild()!!
	}

	@JvmStatic
	fun createProperty(project: Project, key: String, value: String): ParadoxLocalisationProperty {
		return createDummyFile(project, "l_english:\n$key:0 \"$value\"").findChild<ParadoxLocalisationPropertyList>()!!.findChild()!!
	}

	@JvmStatic
	fun createPropertyKey(project: Project, key: String): ParadoxLocalisationPropertyKey {
		return createProperty(project, key, "").findChild()!!
	}

	@JvmStatic
	fun createPropertyValue(project: Project, value: String): ParadoxLocalisationPropertyValue {
		return createProperty(project, "a", value).findChild(forward = false)!!
	}

	@JvmStatic
	fun createPropertyReference(project: Project, name: String): ParadoxLocalisationPropertyReference {
		return createPropertyValue(project, "$$name$").findChild()!!
	}

	@JvmStatic
	fun createIcon(project: Project, name: String): ParadoxLocalisationIcon {
		return createPropertyValue(project, "£$name£").findChild()!!
	}
	
	@JvmStatic
	fun createColorfulText(project: Project, name: String,value:String = ""): ParadoxLocalisationColorfulText {
		return createPropertyValue(project, "§$name$value§!").findChild()!!
	}
	
	@JvmStatic
	fun createCommandExpression(project: Project, text: String): ParadoxLocalisationCommandExpression? {
		return createPropertyValue(project, "[$text]").findChild<ParadoxLocalisationCommand>()!!.findChild()
	}
	
	//@JvmStatic
	//fun createCommandScope(project: Project, name: String): ParadoxLocalisationCommandScope {
	//	val command = createPropertyValue(project, "[$name.GetName]").richTextList.first() as ParadoxLocalisationCommand
	//	return PsiTreeUtil.getChildOfType(command, ParadoxLocalisationCommandScope::class.java)!!
	//}
	//
	//@JvmStatic
	//fun createCommandField(project: Project, name: String): ParadoxLocalisationCommandField {
	//	val command = createPropertyValue(project, "[$name]").richTextList.first() as ParadoxLocalisationCommand
	//	return PsiTreeUtil.getChildOfType(command, ParadoxLocalisationCommandField::class.java)!!
	//}
}
