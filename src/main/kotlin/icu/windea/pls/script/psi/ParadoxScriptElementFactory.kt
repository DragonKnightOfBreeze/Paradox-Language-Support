package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.*

object ParadoxScriptElementFactory {
    @JvmStatic
    fun createDummyFile(project: Project, text: String): ParadoxScriptFile {
        return PsiFileFactory.getInstance(project).createFileFromText(ParadoxScriptLanguage, text)
            .castOrNull<ParadoxScriptFile>() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createLine(project: Project): PsiElement {
        return createDummyFile(project, "\n").firstChild
    }
    
    fun createRootBlock(project: Project, text: String): ParadoxScriptRootBlock {
        return createDummyFile(project, text)
            .findChild<ParadoxScriptRootBlock>() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createScriptedVariable(project: Project, name: String, value: String): ParadoxScriptScriptedVariable {
        return createRootBlock(project, "@$name = $value")
            .findChild<ParadoxScriptScriptedVariable>() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createScriptedVariableName(project: Project, name: String): ParadoxScriptScriptedVariableName {
        return createScriptedVariable(project, name, "0")
            .findChild<ParadoxScriptScriptedVariableName>() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createPropertyFromText(project: Project, text: String): ParadoxScriptProperty {
        return createRootBlock(project, text)
            .findChild<ParadoxScriptProperty>() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createPropertyKey(project: Project, key: String): ParadoxScriptPropertyKey {
        return createPropertyFromText(project, "$key = v")
            .findChild<ParadoxScriptPropertyKey>() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createValue(project: Project, text: String): ParadoxScriptValue {
        return createPropertyFromText(project, "k = $text")
            .findChild<ParadoxScriptValue>() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createString(project: Project, text: String): ParadoxScriptString {
        return createValue(project, text)
            .castOrNull<ParadoxScriptString>() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createBlock(project: Project, text: String): ParadoxScriptBlock {
        return createValue(project, text)
            .castOrNull<ParadoxScriptBlock>() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createVariableReference(project: Project, name: String): ParadoxScriptScriptedVariableReference {
        return createValue(project, "@$name")
            .castOrNull<ParadoxScriptScriptedVariableReference>() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createParameterCondition(project: Project, expression: String, itemsText: String): ParadoxScriptParameterCondition {
        val text = "a = { [[$expression] $itemsText ] }"
        return createRootBlock(project, text)
            .findChild<ParadoxScriptProperty>()
            ?.findChild<ParadoxScriptBlock>()
            ?.findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createParameterConditionParameter(project: Project, name: String): ParadoxScriptParameterConditionParameter {
        return createParameterCondition(project, name, "a")
            .findChild<ParadoxScriptParameterConditionExpression>()
            ?.findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createInlineMath(project: Project, expression: String): ParadoxScriptInlineMath {
        return createValue(project, "@[$expression]")
            .castOrNull() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createInlineMathVariableReference(project: Project, name: String): ParadoxScriptInlineMathScriptedVariableReference {
        return createInlineMath(project, name)
            .findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createParameter(project: Project, text: String): ParadoxScriptParameter {
        return createValue(project, text)
            .findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createParameterSmartly(project: Project, name: String, defaultValue: String? = null): ParadoxScriptParameter {
        val text = if(defaultValue == null) "$$name$" else "$$name|$defaultValue$"
        return createValue(project, text)
            .findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createInlineMathParameter(project: Project, text: String): ParadoxScriptParameter {
        return createInlineMath(project, text)
            .findChild() ?: throw IncorrectOperationException()
    }
    
    @JvmStatic
    fun createInlineMathParameterSmartly(project: Project, name: String, defaultValue: String? = null): ParadoxScriptInlineMathParameter {
        val text = if(defaultValue == null) "$$name$" else "$$name|$defaultValue$"
        return createInlineMath(project, text)
            .findChild() ?: throw IncorrectOperationException()
    }
}
