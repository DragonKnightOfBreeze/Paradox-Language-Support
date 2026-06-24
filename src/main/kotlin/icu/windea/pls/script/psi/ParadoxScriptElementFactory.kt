package icu.windea.pls.script.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiParserFacade
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.findChild
import icu.windea.pls.script.ParadoxScriptLanguage

@Suppress("unused")
object ParadoxScriptElementFactory {
    @JvmStatic
    fun createFileFromText(project: Project, text: String): ParadoxScriptFile {
        return PsiFileFactory.getInstance(project).createFileFromText(ParadoxScriptLanguage, text)
            .castOrNull<ParadoxScriptFile>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createWhiteSpaceFromText(project: Project, text: String): PsiElement {
        return PsiParserFacade.getInstance(project).createWhiteSpaceFromText(text)
    }

    @JvmStatic
    fun createRootBlockFromText(project: Project, text: String): ParadoxScriptRootBlock {
        return createFileFromText(project, text)
            .findChild<ParadoxScriptRootBlock>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createScriptedVariableFromText(project: Project, text: String): ParadoxScriptScriptedVariable {
        return createRootBlockFromText(project, text)
            .findChild<ParadoxScriptScriptedVariable>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createScriptedVariableNameFromText(project: Project, text: String): ParadoxScriptScriptedVariableName {
        return createScriptedVariableFromText(project, "@$text = ${"0"}")
            .findChild<ParadoxScriptScriptedVariableName>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createScriptedVariableValueFromText(project: Project, text: String): ParadoxScriptScriptedVariableName {
        return createScriptedVariableFromText(project, "@${"var"} = $text")
            .findChild<ParadoxScriptScriptedVariableName>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createPropertyFromText(project: Project, text: String): ParadoxScriptProperty {
        return createRootBlockFromText(project, text)
            .findChild<ParadoxScriptProperty>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createPropertyKeyFromText(project: Project, text: String): ParadoxScriptPropertyKey {
        return createPropertyFromText(project, "$text = v")
            .findChild<ParadoxScriptPropertyKey>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createValueFromText(project: Project, text: String): ParadoxScriptValue {
        return createPropertyFromText(project, "k = $text")
            .findChild<ParadoxScriptValue>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createStringFromText(project: Project, text: String): ParadoxScriptString {
        return createValueFromText(project, text)
            .castOrNull<ParadoxScriptString>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createBlockFromText(project: Project, text: String): ParadoxScriptBlock {
        return createValueFromText(project, text)
            .castOrNull<ParadoxScriptBlock>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createScriptedVariableReferenceFromText(project: Project, text: String): ParadoxScriptScriptedVariableReference {
        return createValueFromText(project, text)
            .castOrNull<ParadoxScriptScriptedVariableReference>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createScriptedVariableReference(project: Project, name: String): ParadoxScriptScriptedVariableReference {
        return createScriptedVariableReferenceFromText(project, "@$name")
    }

    @JvmStatic
    fun createConditionalBlockFromText(project: Project, text: String): ParadoxScriptConditionalBlock {
        return createRootBlockFromText(project, "a = { $text }")
            .findChild<ParadoxScriptProperty>()
            ?.findChild<ParadoxScriptBlock>()
            ?.findChild<ParadoxScriptConditionalBlock>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createConditionalBlock(project: Project, expression: String, itemsText: String): ParadoxScriptConditionalBlock {
        return createConditionalBlockFromText(project, "[[$expression] $itemsText ]")
    }

    @JvmStatic
    fun createConditionalBlockParameterFromText(project: Project, text: String): ParadoxScriptConditionalBlockParameter {
        return createConditionalBlock(project, text, "a")
            .findChild<ParadoxScriptConditionalBlockExpression>()
            ?.findChild<ParadoxScriptConditionalBlockParameter>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createConditionalBlockParameter(project: Project, name: String): ParadoxScriptConditionalBlockParameter {
        return createConditionalBlockParameterFromText(project, name)
    }

    @JvmStatic
    fun createInlineMathFromText(project: Project, text: String): ParadoxScriptInlineMath {
        return createValueFromText(project, text)
            .castOrNull() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createInlineMath(project: Project, expression: String): ParadoxScriptInlineMath {
        return createInlineMathFromText(project, "@[$expression]")
    }

    @JvmStatic
    fun createInlineMathScriptedVariableReferenceFromText(project: Project, name: String): ParadoxScriptInlineMathScriptedVariableReference {
        return createInlineMath(project, name)
            .findChild<ParadoxScriptInlineMathScriptedVariableReference>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createInlineMathScriptedVariableReference(project: Project, name: String): ParadoxScriptInlineMathScriptedVariableReference {
        return createInlineMathScriptedVariableReferenceFromText(project, name)
    }

    @JvmStatic
    fun createParameterFromText(project: Project, text: String): ParadoxScriptParameter {
        return createValueFromText(project, text)
            .findChild<ParadoxScriptParameter>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createParameter(project: Project, name: String, defaultValue: String? = null): ParadoxScriptParameter {
        val text = if (defaultValue == null) "$$name$" else "$$name|$defaultValue$"
        return createParameterFromText(project, text)
    }

    @JvmStatic
    fun createInlineMathParameterFromText(project: Project, text: String): ParadoxScriptInlineMathParameter {
        return createInlineMath(project, text)
            .findChild<ParadoxScriptInlineMathParameter>() ?: throw IncorrectOperationException()
    }

    @JvmStatic
    fun createInlineMathParameter(project: Project, name: String, defaultValue: String? = null): ParadoxScriptInlineMathParameter {
        val text = if (defaultValue == null) "$$name$" else "$$name|$defaultValue$"
        return createInlineMathParameterFromText(project, text)
    }
}
