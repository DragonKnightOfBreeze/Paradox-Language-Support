package icu.windea.pls.script.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.impl.*

val ParadoxParameter.defaultValueToken: PsiElement?
    get() = findChild { it.elementType == PARAMETER_VALUE_TOKEN }

val ParadoxScriptPropertyKey.propertyValue: ParadoxScriptValue?
    get() = siblings(forward = true, withSelf = false).findIsInstance()

val ParadoxScriptValue.propertyKey: ParadoxScriptPropertyKey?
    get() = siblings(forward = false, withSelf = false).findIsInstance()

val ParadoxScriptScriptedVariable.greenStub: ParadoxScriptScriptedVariableStub?
    get() = this.castOrNull<ParadoxScriptScriptedVariableImpl>()?.greenStub

val ParadoxScriptProperty.greenStub: ParadoxScriptPropertyStub?
    get() = this.castOrNull<ParadoxScriptPropertyImpl>()?.greenStub
