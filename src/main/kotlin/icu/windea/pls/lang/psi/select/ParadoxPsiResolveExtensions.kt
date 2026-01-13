package icu.windea.pls.lang.psi.select

import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.references.ParadoxScriptedVariablePsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationParameterPsiReference
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptColor
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInt
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.booleanValue
import icu.windea.pls.script.psi.floatValue
import icu.windea.pls.script.psi.intValue
import icu.windea.pls.script.psi.isValidExpression
import icu.windea.pls.script.psi.stringValue
import java.awt.Color

// PSI Resolve Extensions

fun ParadoxLocalisationParameter.resolveLocalisation(): ParadoxLocalisationProperty? {
    return reference?.castOrNull<ParadoxLocalisationParameterPsiReference>()?.resolveLocalisation()
}

fun ParadoxScriptedVariableReference.resolveScriptedVariable(): ParadoxScriptScriptedVariable? {
    return reference?.castOrNull<ParadoxScriptedVariablePsiReference>()?.resolve()
}

fun ParadoxLocalisationParameter.resolveScriptedVariable(): ParadoxScriptScriptedVariable? {
    return scriptedVariableReference?.reference?.castOrNull<ParadoxScriptedVariablePsiReference>()?.resolve()
}

fun ParadoxScriptedVariableReference.resolved(): ParadoxScriptValue? {
    return this.resolveScriptedVariable()?.scriptedVariableValue
}

fun <T : ParadoxScriptExpressionElement> T.resolved(): ParadoxScriptExpressionElement? {
    return when (this) {
        is ParadoxScriptScriptedVariableReference -> this.resolveScriptedVariable()?.scriptedVariableValue
        else -> this
    }
}

// Value Resolve Extensions

fun ParadoxScriptMember.selectValue(): String? {
    return when (this) {
        is ParadoxScriptProperty -> this.value
        is ParadoxScriptBlock -> null
        is ParadoxScriptColor -> null
        is ParadoxScriptInlineMath -> null
        is ParadoxScriptValue -> this.value
        else -> null
    }
}

fun ParadoxScriptExpressionElement.value(valid: Boolean = false): String? {
    if (valid && !this.isValidExpression()) return null
    val resolved = this.resolved() ?: return null
    return resolved.value
}

fun ParadoxScriptExpressionElement.booleanValue(valid: Boolean = false): Boolean? {
    if (this !is ParadoxScriptValue) return null
    if (valid && !this.isValidExpression()) return null
    val resolved = this.resolved() ?: return null
    return when (resolved) {
        is ParadoxScriptBoolean -> resolved.booleanValue
        else -> null
    }
}

fun ParadoxScriptExpressionElement.intValue(valid: Boolean = false): Int? {
    if (valid && !this.isValidExpression()) return null
    val resolved = this.resolved() ?: return null
    return when (resolved) {
        is ParadoxScriptPropertyKey -> resolved.value.toIntOrNull()
        is ParadoxScriptInt -> resolved.intValue
        is ParadoxScriptFloat -> resolved.floatValue.toInt()
        is ParadoxScriptString -> resolved.value.toIntOrNull()
        else -> null
    }
}

fun ParadoxScriptExpressionElement.floatValue(valid: Boolean = false): Float? {
    if (valid && !this.isValidExpression()) return null
    val resolved = this.resolved() ?: return null
    return when (resolved) {
        is ParadoxScriptPropertyKey -> resolved.value.toFloatOrNull()
        is ParadoxScriptInt -> resolved.intValue.toFloat()
        is ParadoxScriptFloat -> resolved.floatValue
        is ParadoxScriptString -> resolved.value.toFloatOrNull()
        else -> null
    }
}

fun ParadoxScriptExpressionElement.stringValue(valid: Boolean = false): String? {
    if (valid && !this.isValidExpression()) return null
    val resolved = this.resolved() ?: return null
    return when (resolved) {
        is ParadoxScriptPropertyKey -> resolved.value
        is ParadoxScriptString -> resolved.value
        is ParadoxScriptInt -> resolved.value
        is ParadoxScriptFloat -> resolved.value
        else -> null
    }
}

fun ParadoxScriptValue.colorValue(valid: Boolean = false): Color? {
    if (valid && !this.isValidExpression()) return null
    return when (this) {
        is ParadoxScriptColor -> this.color
        else -> null
    }
}

fun ParadoxScriptValue.resolveValue(valid: Boolean = false): Any? {
    if (valid && !this.isValidExpression()) return null
    return when (this) {
        is ParadoxScriptBoolean -> this.booleanValue
        is ParadoxScriptInt -> this.intValue
        is ParadoxScriptFloat -> this.floatValue
        is ParadoxScriptString -> this.stringValue
        is ParadoxScriptColor -> this.color
        is ParadoxScriptScriptedVariableReference -> this.resolveScriptedVariable()?.scriptedVariableValue?.resolveValue()
        is ParadoxScriptBlock -> null // unsupported
        else -> null // unsupported
    }
}
