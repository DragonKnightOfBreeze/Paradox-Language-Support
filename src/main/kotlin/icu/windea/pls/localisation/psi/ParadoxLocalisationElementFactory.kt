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
            ?.findChild<ParadoxLocalisationProperty>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createPropertyKey(project: Project, key: String): ParadoxLocalisationPropertyKey {
        return createProperty(project, key, "")
            .findChild<ParadoxLocalisationPropertyKey>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createPropertyValue(project: Project, value: String): ParadoxLocalisationPropertyValue {
        return createProperty(project, "a", value)
            .findChild<ParadoxLocalisationPropertyValue>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createString(project: Project, text: String): ParadoxLocalisationString {
        return createPropertyValue(project, text).tokenElement
            ?.findChild<ParadoxLocalisationString>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createColorfulText(project: Project, name: String, value: String = ""): ParadoxLocalisationColorfulText {
        return createPropertyValue(project, "§$name$value§!").tokenElement
            ?.findChild<ParadoxLocalisationColorfulText>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createPropertyReference(project: Project, name: String): ParadoxLocalisationPropertyReference {
        return createPropertyValue(project, "$$name$").tokenElement
            ?.findChild<ParadoxLocalisationPropertyReference>() ?: throw IncorrectOperationException()
    }

    fun createScriptedVariableReference(project: Project, name: String): ParadoxLocalisationScriptedVariableReference {
        return createPropertyValue(project, "$@$name$").tokenElement
            ?.findChild<ParadoxLocalisationPropertyReference>()
            ?.findChild<ParadoxLocalisationScriptedVariableReference>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createCommandText(project: Project, text: String): ParadoxLocalisationCommandText {
        return createPropertyValue(project, "[$text]").tokenElement
            ?.findChild<ParadoxLocalisationCommand>()
            ?.findChild<ParadoxLocalisationCommandText>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createIcon(project: Project, name: String): ParadoxLocalisationIcon {
        return createPropertyValue(project, "£$name£").tokenElement
            ?.findChild<ParadoxLocalisationIcon>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createConceptName(project: Project, name: String): ParadoxLocalisationConceptName {
        return createPropertyValue(project, "['$name']").tokenElement
            ?.findChild<ParadoxLocalisationCommand>()
            ?.findChild<ParadoxLocalisationConceptName>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createTextFormat(project: Project, name: String): ParadoxLocalisationTextFormat {
        return createPropertyValue(project, "#$name #!").tokenElement
            ?.findChild<ParadoxLocalisationTextFormat>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createTextIcon(project: Project, name: String): ParadoxLocalisationTextIcon {
        return createPropertyValue(project, "@$name!").tokenElement
            ?.findChild<ParadoxLocalisationTextIcon>() ?: throw IncorrectOperationException()
    }
}
