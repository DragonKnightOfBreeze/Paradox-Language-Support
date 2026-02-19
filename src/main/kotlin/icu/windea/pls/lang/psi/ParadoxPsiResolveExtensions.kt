@file:Suppress("unused")

package icu.windea.pls.lang.psi

import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.references.ParadoxScriptedVariablePsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationParameterPsiReference
import icu.windea.pls.lang.util.evaluators.MathResult
import icu.windea.pls.lang.util.evaluators.ParadoxInlineMathEvaluator
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptColor
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInt
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.booleanValue
import icu.windea.pls.script.psi.floatValue
import icu.windea.pls.script.psi.intValue
import icu.windea.pls.script.psi.isValidExpression
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

fun ParadoxScriptMember.selectLiteralValue(): String? {
    return when (this) {
        is ParadoxScriptProperty -> this.propertyValue?.selectLiteralValue()
        is ParadoxScriptBoolean -> this.value
        is ParadoxScriptInt -> this.value
        is ParadoxScriptFloat -> this.value
        is ParadoxScriptString -> this.value
        else -> null
    }
}

fun ParadoxScriptExpressionElement.value(valid: Boolean = false): String? {
    val resolved = resolved() ?: return null
    val r = resolved.value
    if (valid && !isValidExpression()) return null
    return r
}

fun ParadoxScriptExpressionElement.booleanValue(strict: Boolean = false, valid: Boolean = false): Boolean? {
    if (this !is ParadoxScriptValue) return null
    val resolved = resolved() ?: return null
    if (strict && resolved !is ParadoxScriptBoolean) return null
    val r = when (resolved) {
        is ParadoxScriptBoolean -> resolved.booleanValue
        else -> true
    }
    if (valid && !isValidExpression()) return null
    return r
}

fun ParadoxScriptExpressionElement.intValue(strict: Boolean = false, valid: Boolean = false): Int? {
    val resolved = resolved() ?: return null
    if (strict && this !is ParadoxScriptInt) return null
    val r = when (resolved) {
        is ParadoxScriptInt -> resolved.intValue
        is ParadoxScriptFloat -> resolved.floatValue.toInt()
        is ParadoxScriptStringExpressionElement -> resolved.value.toIntOrNull()
        else -> null
    }
    if (r == null) return null
    if (valid && !isValidExpression()) return null
    return r
}

fun ParadoxScriptExpressionElement.floatValue(strict: Boolean = false, valid: Boolean = false): Float? {
    val resolved = resolved() ?: return null
    if (strict && this !is ParadoxScriptFloat) return null
    val r = when (resolved) {
        is ParadoxScriptFloat -> resolved.floatValue
        is ParadoxScriptInt -> resolved.intValue.toFloat()
        is ParadoxScriptStringExpressionElement -> resolved.value.toFloatOrNull()
        else -> null
    }
    if (r == null) return null
    if (valid && !isValidExpression()) return null
    return r
}

fun ParadoxScriptExpressionElement.stringValue(strict: Boolean = false, valid: Boolean = false): String? {
    val resolved = resolved() ?: return null
    if (strict && this !is ParadoxScriptStringExpressionElement) return null
    val r = when (resolved) {
        is ParadoxScriptStringExpressionElement -> resolved.value
        is ParadoxScriptBoolean -> resolved.value
        is ParadoxScriptInt -> resolved.value
        is ParadoxScriptFloat -> resolved.value
        else -> null
    }
    if (r == null) return null
    if (valid && !isValidExpression()) return null
    return r
}

fun ParadoxScriptValue.colorValue(valid: Boolean = false): Color? {
    if (this !is ParadoxScriptColor) return null
    val r = this.color ?: return null
    if (valid && !isValidExpression()) return null
    return r
}

fun ParadoxScriptValue.inlineMathValue(valid: Boolean = false): MathResult? {
    if (this !is ParadoxScriptInlineMath) return null
    val r = runCatching { ParadoxInlineMathEvaluator().evaluate(this) }.getOrNull() ?: return null
    if (valid && !isValidExpression()) return null
    return r
}

fun ParadoxScriptValue.evaluateValue(strict: Boolean = false, valid: Boolean = false): Any? {
    val resolved = resolved() ?: return null
    val r = when (resolved) {
        is ParadoxScriptBoolean -> resolved.booleanValue(strict, valid)
        is ParadoxScriptInt -> resolved.intValue(strict, valid)
        is ParadoxScriptFloat -> resolved.floatValue(strict, valid)
        is ParadoxScriptString -> resolved.stringValue(strict, valid)
        is ParadoxScriptColor -> resolved.colorValue(valid)
        is ParadoxScriptInlineMath -> resolved.inlineMathValue(valid)
        else -> null // unsupported
    }
    return r
}
