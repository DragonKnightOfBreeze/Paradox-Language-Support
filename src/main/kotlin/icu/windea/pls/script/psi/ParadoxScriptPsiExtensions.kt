package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

val ParadoxScriptVariableName.variableNameId: PsiElement get() = findRequiredChild(VARIABLE_NAME_ID)

val ParadoxScriptPropertyKey.propertyKeyId: PsiElement? get() = findChild(PROPERTY_KEY_ID)

val ParadoxScriptPropertyKey.quotedPropertyKeyId: PsiElement? get() = findChild(QUOTED_PROPERTY_KEY_ID)

val ParadoxScriptVariableReference.variableReferenceId: PsiElement get() = findRequiredChild(VARIABLE_REFERENCE_ID)