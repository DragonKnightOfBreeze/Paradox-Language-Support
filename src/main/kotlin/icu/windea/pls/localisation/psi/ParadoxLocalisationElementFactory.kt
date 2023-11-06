package icu.windea.pls.localisation.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.*

object ParadoxLocalisationElementFactory {
    @JvmStatic
    fun createDummyFile(project: Project, text: String): ParadoxLocalisationFile {
        return PsiFileFactory.getInstance(project).createFileFromText(ParadoxLocalisationLanguage, text) as ParadoxLocalisationFile
    }
    
    @JvmStatic
    fun createLocale(project: Project, locale: String): ParadoxLocalisationLocale {
        return createDummyFile(project, "$locale:\n").propertyList?.locale!!
    }
    
    @JvmStatic
    fun createProperty(project: Project, key: String, value: String): ParadoxLocalisationProperty {
        return createDummyFile(project, "l_english:\n$key:0 \"$value\"")
            .findChild<ParadoxLocalisationPropertyList>()!!
            .findChild()!!
    }
    
    @JvmStatic
    fun createPropertyKey(project: Project, key: String): ParadoxLocalisationPropertyKey {
        return createProperty(project, key, "")
            .findChild()!!
    }
    
    @JvmStatic
    fun createPropertyValue(project: Project, value: String): ParadoxLocalisationPropertyValue {
        return createProperty(project, "a", value)
            .findChild()!!
    }
    
    @JvmStatic
    fun createPropertyReference(project: Project, name: String): ParadoxLocalisationPropertyReference {
        return createPropertyValue(project, "$$name$")
            .findChild()!!
    }
    
    fun createScriptedVariableReference(project: Project, name: String): ParadoxLocalisationScriptedVariableReference {
        return createPropertyValue(project, "$@$name$")
            .findChild<ParadoxLocalisationPropertyReference>()!!
            .findChild()!!
    }
    
    @JvmStatic
    fun createIcon(project: Project, name: String): ParadoxLocalisationIcon {
        return createPropertyValue(project, "£$name£")
            .findChild()!!
    }
    
    @JvmStatic
    fun createColorfulText(project: Project, name: String, value: String = ""): ParadoxLocalisationColorfulText {
        return createPropertyValue(project, "§$name$value§!")
            .findChild()!!
    }
    
    @JvmStatic
    fun createCommandScope(project: Project, name: String): ParadoxLocalisationCommandScope {
        return createPropertyValue(project, "[$name.GetName]")
            .findChild<ParadoxLocalisationCommand>()!!
            .findChild()!!
    }
    
    @JvmStatic
    fun createCommandField(project: Project, name: String): ParadoxLocalisationCommandField {
        return createPropertyValue(project, "[$name]")
            .findChild<ParadoxLocalisationCommand>()!!
            .findChild()!!
    }
    
    @JvmStatic
    fun createConceptName(project: Project, name: String): ParadoxLocalisationConceptName {
        return createPropertyValue(project, "['$name']")
            .findChild<ParadoxLocalisationCommand>()!!
            .findChild()!!
    }
}
