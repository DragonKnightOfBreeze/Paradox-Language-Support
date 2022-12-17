package icu.windea.pls.script.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*

val ParadoxScriptScriptedVariableName.variableNameId: PsiElement get() = findChild(ParadoxScriptElementTypes.SCRIPTED_VARIABLE_NAME_ID)!!

val ParadoxScriptPropertyKey.propertyKeyId: PsiElement? get() = findChild(ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN)

val ParadoxScriptPropertyKey.quotedPropertyKeyId: PsiElement? get() = findChild(ParadoxScriptElementTypes.QUOTED_PROPERTY_KEY_TOKEN)

val ParadoxScriptScriptedVariableReference.variableReferenceId: PsiElement get() = findChild(ParadoxScriptElementTypes.SCRIPTED_VARIABLE_REFERENCE_ID)!!

val ParadoxScriptParameterConditionParameter.parameterId: PsiElement get() = findChild(ParadoxScriptElementTypes.ARGUMENT_ID)!!

val ParadoxScriptInlineMathScriptedVariableReference.variableReferenceId: PsiElement get() = findChild(ParadoxScriptElementTypes.INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_ID)!!

val ParadoxParameter.parameterId: PsiElement? get() = findChild(ParadoxScriptElementTypes.PARAMETER_ID)

val ParadoxParameter.defaultValueToken: PsiElement?
	get() = when {
		this is ParadoxScriptParameter -> findChild(ParadoxScriptTokenSets.parameterValueTokens)
		this is ParadoxScriptInlineMathParameter -> findChild(ParadoxScriptTokenSets.inlineMathParameterValueTokens)
		else -> null
	}

val ParadoxScriptPropertyKey.propertyValue: ParadoxScriptValue? get() = siblings(forward = true, withSelf = false).findIsInstance()

val ParadoxScriptValue.propertyKey: ParadoxScriptPropertyKey? get() = siblings(forward = false, withSelf = false).findIsInstance()