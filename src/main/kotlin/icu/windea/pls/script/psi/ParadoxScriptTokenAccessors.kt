package icu.windea.pls.script.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

val ParadoxScriptScriptedVariableName.variableNameId: PsiElement? get() = firstChild?.nextSibling?.takeIf { it.nextSibling == null && it.elementType == SCRIPTED_VARIABLE_NAME_TOKEN }

val ParadoxScriptPropertyKey.propertyKeyId: PsiElement? get() = findChild(PROPERTY_KEY_TOKEN)

val ParadoxScriptPropertyKey.quotedPropertyKeyId: PsiElement? get() = findChild(QUOTED_PROPERTY_KEY_TOKEN)

val ParadoxScriptScriptedVariableReference.variableReferenceId: PsiElement? get() = firstChild?.nextSibling?.takeIf { it.nextSibling == null && it.elementType == SCRIPTED_VARIABLE_REFERENCE_TOKEN }

val ParadoxScriptParameterConditionParameter.parameterId: PsiElement get() = findChild(ARGUMENT_ID)!!

val ParadoxScriptInlineMathScriptedVariableReference.variableReferenceId: PsiElement? get() = firstChild?.nextSibling?.takeIf { it.nextSibling == null && it.elementType == INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN }

val ParadoxParameter.parameterId: PsiElement? get() = findChild(PARAMETER_TOKEN)

val ParadoxParameter.defaultValueToken: PsiElement?
	get() = when {
		this is ParadoxScriptParameter -> findChild(ParadoxScriptTokenSets.PARAMETER_VALUE_TOKENS)
		this is ParadoxScriptInlineMathParameter -> findChild(ParadoxScriptTokenSets.INLINE_MATH_PARAMETER_VALUE_TOKENS)
		else -> null
	}

val ParadoxScriptPropertyKey.propertyValue: ParadoxScriptValue? get() = siblings(forward = true, withSelf = false).findIsInstance()

val ParadoxScriptValue.propertyKey: ParadoxScriptPropertyKey? get() = siblings(forward = false, withSelf = false).findIsInstance()