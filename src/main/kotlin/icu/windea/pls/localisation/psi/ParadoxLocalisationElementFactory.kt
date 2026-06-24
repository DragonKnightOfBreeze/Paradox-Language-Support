package icu.windea.pls.localisation.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiParserFacade
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.findChild
import icu.windea.pls.localisation.ParadoxLocalisationLanguage

@Suppress("unused")
object ParadoxLocalisationElementFactory {
    @JvmStatic
    fun createFileFromText(project: Project, text: String): ParadoxLocalisationFile {
        return PsiFileFactory.getInstance(project).createFileFromText(ParadoxLocalisationLanguage, text)
            .castOrNull() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createWhiteSpaceFromText(project: Project, text: String): PsiElement {
        return PsiParserFacade.getInstance(project).createWhiteSpaceFromText(text)
    }

    @JvmStatic
    fun createLocale(project: Project, locale: String): ParadoxLocalisationLocale {
        return createFileFromText(project, "$locale:\n")
            .propertyList?.locale ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createPropertyFromText(project: Project, text: String): ParadoxLocalisationProperty {
        return createFileFromText(project, "l_english:\n$text")
            .findChild<ParadoxLocalisationPropertyList>()
            ?.findChild<ParadoxLocalisationProperty>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createProperty(project: Project, key: String, value: String): ParadoxLocalisationProperty {
        return createPropertyFromText(project, "$key:0 \"$value\"")
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
    fun createText(project: Project, text: String): ParadoxLocalisationText {
        return createPropertyValue(project, text).tokenElement
            ?.findChild<ParadoxLocalisationText>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createColorfulText(project: Project, name: String, value: String = ""): ParadoxLocalisationColorfulText {
        return createPropertyValue(project, "§$name$value§!").tokenElement
            ?.findChild<ParadoxLocalisationColorfulText>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createParameter(project: Project, name: String): ParadoxLocalisationParameter {
        return createPropertyValue(project, "$$name$").tokenElement
            ?.findChild<ParadoxLocalisationParameter>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createScriptedVariableReference(project: Project, name: String): ParadoxLocalisationScriptedVariableReference {
        return createPropertyValue(project, "$@$name$").tokenElement
            ?.findChild<ParadoxLocalisationParameter>()
            ?.findChild<ParadoxLocalisationScriptedVariableReference>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createIcon(project: Project, name: String): ParadoxLocalisationIcon {
        return createPropertyValue(project, "£$name£").tokenElement
            ?.findChild<ParadoxLocalisationIcon>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createCommandText(project: Project, text: String): ParadoxLocalisationCommandText {
        return createPropertyValue(project, "[$text]").tokenElement
            ?.findChild<ParadoxLocalisationCommand>()
            ?.findChild<ParadoxLocalisationCommandText>() ?: throw IncorrectOperationException()
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
