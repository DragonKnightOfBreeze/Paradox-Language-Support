package icu.windea.pls.localisation.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.*

object ParadoxLocalisationElementFactory {
    @JvmStatic
    fun createDummyFile(project: Project, text: String): ParadoxLocalisationFile {
        return PsiFileFactory.getInstance(project).createFileFromText(ParadoxLocalisationLanguage, text)
            .castOrNull() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createLocale(project: Project, locale: String): ParadoxLocalisationLocale {
        return createDummyFile(project, "$locale:\n")
            .propertyList?.locale ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createProperty(project: Project, key: String, value: String): ParadoxLocalisationProperty {
        return createDummyFile(project, "l_english:\n$key:0 \"$value\"")
            .findChild<ParadoxLocalisationPropertyList>()
            ?.findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createPropertyKey(project: Project, key: String): ParadoxLocalisationPropertyKey {
        return createProperty(project, key, "")
            .findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createPropertyValue(project: Project, value: String): ParadoxLocalisationPropertyValue {
        return createProperty(project, "a", value)
            .findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createPropertyReference(project: Project, name: String): ParadoxLocalisationPropertyReference {
        return createPropertyValue(project, "$$name$")
            .findChild() ?: throw IncorrectOperationException()
    }
    
    fun createScriptedVariableReference(project: Project, name: String): ParadoxLocalisationScriptedVariableReference {
        return createPropertyValue(project, "$@$name$")
            .findChild<ParadoxLocalisationPropertyReference>()
            ?.findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createCommandScope(project: Project, name: String): ParadoxLocalisationCommandScope {
        return createPropertyValue(project, "[$name.GetName]")
            .findChild<ParadoxLocalisationCommand>()
            ?.findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createCommandField(project: Project, name: String): ParadoxLocalisationCommandField {
        return createPropertyValue(project, "[$name]")
            .findChild<ParadoxLocalisationCommand>()
            ?.findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createConceptName(project: Project, name: String): ParadoxLocalisationConceptName {
        return createPropertyValue(project, "['$name']")
            .findChild<ParadoxLocalisationCommand>()
            ?.findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createIcon(project: Project, name: String): ParadoxLocalisationIcon {
        return createPropertyValue(project, "£$name£")
            .findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createColorfulText(project: Project, name: String, value: String = ""): ParadoxLocalisationColorfulText {
        return createPropertyValue(project, "§$name$value§!")
            .findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createString(project: Project, text: String): ParadoxLocalisationString {
        return createPropertyValue(project, text)
            .findChild() ?: throw IncorrectOperationException()
    }
}
