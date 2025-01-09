package icu.windea.pls.script.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.impl.*

val ParadoxScriptScriptedVariableName.idElement: PsiElement?
    get() = firstChild?.nextSibling?.takeIf { it.nextSibling == null && it.elementType == SCRIPTED_VARIABLE_NAME_TOKEN }

val ParadoxScriptPropertyKey.idElement: PsiElement?
    get() = firstChild?.takeIf { it.nextSibling == null && it.elementType == PROPERTY_KEY_TOKEN }

val ParadoxScriptScriptedVariableReference.idElement: PsiElement?
    get() = firstChild?.nextSibling?.takeIf { it.nextSibling == null && it.elementType == SCRIPTED_VARIABLE_REFERENCE_TOKEN }

val ParadoxScriptString.idElement: PsiElement?
    get() = firstChild?.takeIf { it.nextSibling == null && it.elementType == STRING_TOKEN }

val ParadoxScriptParameterConditionParameter.idElement: PsiElement
    get() = findChild(CONDITION_PARAMETER_TOKEN)!!

val ParadoxScriptInlineMathScriptedVariableReference.idElement: PsiElement?
    get() = firstChild?.takeIf { it.nextSibling == null && it.elementType == INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN }

val ParadoxScriptParameter.idElement: PsiElement?
    get() = findChild(PARAMETER_TOKEN)

val ParadoxScriptInlineMathParameter.idElement: PsiElement?
    get() = findChild(PARAMETER_TOKEN)

val ParadoxParameter.defaultValueToken: PsiElement?
    get() = findChild(PARAMETER_VALUE_TOKEN)

val ParadoxScriptPropertyKey.propertyValue: ParadoxScriptValue?
    get() = siblings(forward = true, withSelf = false).findIsInstance()

val ParadoxScriptValue.propertyKey: ParadoxScriptPropertyKey?
    get() = siblings(forward = false, withSelf = false).findIsInstance()

val ParadoxScriptScriptedVariable.greenStub: ParadoxScriptScriptedVariableStub?
    get() = this.castOrNull<ParadoxScriptScriptedVariableImpl>()?.greenStub

@Suppress("UNCHECKED_CAST")
val <T : ParadoxScriptDefinitionElement> T.greenStub: ParadoxScriptDefinitionElementStub<T>?
    get() = when {
        this is ParadoxScriptFile -> this.stub
        this is ParadoxScriptPropertyImpl -> this.greenStub
        else -> throw IllegalStateException()
    } as? ParadoxScriptDefinitionElementStub<T>?

val ParadoxScriptProperty.greenStub: ParadoxScriptPropertyStub?
    get() = this.castOrNull<ParadoxScriptPropertyImpl>()?.greenStub
