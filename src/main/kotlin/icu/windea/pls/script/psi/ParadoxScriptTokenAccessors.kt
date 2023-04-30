package icu.windea.pls.script.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

val ParadoxScriptScriptedVariableName.idElement: PsiElement? get() = firstChild?.nextSibling?.takeIf { it.elementType == SCRIPTED_VARIABLE_NAME_TOKEN }

val ParadoxScriptPropertyKey.idElement: PsiElement? get() = firstChild?.takeIf { it.elementType == PROPERTY_KEY_TOKEN || it.elementType == QUOTED_PROPERTY_KEY_TOKEN }

val ParadoxScriptScriptedVariableReference.idElement: PsiElement? get() = firstChild?.nextSibling?.takeIf { it.elementType == SCRIPTED_VARIABLE_REFERENCE_TOKEN }

val ParadoxScriptString.idElement: PsiElement? get() = firstChild?.takeIf { it.elementType == STRING_TOKEN || it.elementType == QUOTED_STRING_TOKEN }

val ParadoxScriptParameterConditionParameter.idElement: PsiElement get() = findChild(ARGUMENT_TOKEN)!!

val ParadoxScriptInlineMathScriptedVariableReference.idElement: PsiElement? get() = firstChild?.nextSibling?.takeIf { it.nextSibling == null && it.elementType == INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN }

val ParadoxScriptParameter.idElement: PsiElement? get() = findChild(PARAMETER_TOKEN)

val ParadoxScriptInlineMathParameter.idElement: PsiElement? get() = findChild(PARAMETER_TOKEN)

val ParadoxParameter.defaultValueToken: PsiElement?
	get() = when {
		this is ParadoxScriptInlineMathParameter -> findChild(ParadoxScriptTokenSets.INLINE_MATH_PARAMETER_VALUE_TOKENS)
		else -> findChild(ParadoxScriptTokenSets.PARAMETER_VALUE_TOKENS)
	}

val ParadoxScriptPropertyKey.propertyValue: ParadoxScriptValue? get() = siblings(forward = true, withSelf = false).findIsInstance()

val ParadoxScriptValue.propertyKey: ParadoxScriptPropertyKey? get() = siblings(forward = false, withSelf = false).findIsInstance()