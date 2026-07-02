@file:Suppress("unused")

package icu.windea.pls.lang.psi

import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.math.MathResult
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.normalized
import icu.windea.pls.lang.references.ParadoxScriptedVariablePsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationParameterPsiReference
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.evaluators.ParadoxInlineMathExpressionEvaluator
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptColor
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInt
import icu.windea.pls.script.psi.ParadoxScriptNumberExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.booleanValue
import icu.windea.pls.script.psi.floatValue
import icu.windea.pls.script.psi.intValue
import icu.windea.pls.script.psi.parentBlock
import icu.windea.pls.script.psi.parentProperty
import java.awt.Color

// region PSI Predicates

fun ParadoxScriptExpressionElement.isResolvableLiteralExpression(): Boolean {
    return this is ParadoxScriptStringExpressionElement || this is ParadoxScriptNumberExpressionElement
}

/**
 * 判断当前字符串表达式是否在顶层或者子句中或者作为属性的值，并且拥有唯一匹配的规则。
 */
fun ParadoxScriptExpressionElement.isValidExpression(options: ParadoxMatchOptions? = null): Boolean {
    return ParadoxConfigManager.getConfigs(this, options.normalized().copy(fallback = false)).size == 1
}

fun ParadoxScriptExpressionElement.isDefinitionTypeKeyOrName(): Boolean {
    return when {
        this is ParadoxScriptPropertyKey -> isDefinitionTypeKey()
        this is ParadoxScriptValue -> isDefinitionName()
        else -> false
    }
}

fun ParadoxScriptPropertyKey.isDefinitionTypeKey(): Boolean {
    val definition = parentProperty ?: return false
    if (definition.definitionInfo != null) return true
    return false
}

fun ParadoxScriptValue.isDefinitionName(): Boolean {
    // #131
    if (!isResolvableLiteralExpression()) return false

    val nameProperty = parentProperty ?: return false
    // def = def_name
    if (nameProperty.definitionInfo.let { it != null && it.typeConfig.nameField == "" }) return true
    val block = nameProperty.parentBlock ?: return false
    val definition = block.parentProperty ?: return false
    // def = { name_prop = def_name }
    if (definition.definitionInfo.let { it != null && it.typeConfig.nameField == nameProperty.name }) return true
    return false
}

// endregion

// region PSI Resolve Extensions

fun ParadoxLocalisationParameter.resolveLocalisation(): ParadoxLocalisationProperty? {
    return reference?.castOrNull<ParadoxLocalisationParameterPsiReference>()?.resolveLocalisation()
}

fun ParadoxScriptedVariableReference.resolveScriptedVariable(): ParadoxScriptScriptedVariable? {
    return reference?.castOrNull<ParadoxScriptedVariablePsiReference>()?.resolve()
}

fun ParadoxLocalisationParameter.resolveScriptedVariable(): ParadoxScriptScriptedVariable? {
    return scriptedVariableReference?.reference?.castOrNull<ParadoxScriptedVariablePsiReference>()?.resolve()
}

fun <T : ParadoxScriptedVariableReference> T.resolved(): ParadoxScriptValue? {
    return this.resolveScriptedVariable()?.scriptedVariableValue
}

fun <T : ParadoxScriptValue> T.resolved(): ParadoxScriptValue? {
    return when (this) {
        is ParadoxScriptScriptedVariableReference -> this.resolveScriptedVariable()?.scriptedVariableValue
        else -> this
    }
}

fun <T : ParadoxScriptExpressionElement> T.resolved(): ParadoxScriptExpressionElement? {
    return when (this) {
        is ParadoxScriptScriptedVariableReference -> this.resolveScriptedVariable()?.scriptedVariableValue
        else -> this
    }
}

// endregion

// region Value Resolve Extensions

fun ParadoxScriptExpressionElement.value(valid: Boolean = false): String? {
    val resolved = resolved() ?: return null
    val r = resolved.value
    if (valid && !isValidExpression()) return null
    return r
}

fun ParadoxScriptExpressionElement.booleanValue(strict: Boolean = false, valid: Boolean = false): Boolean? {
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
    val r = runCatching { ParadoxInlineMathExpressionEvaluator().evaluate(this) }.getOrNull() ?: return null
    if (valid && !isValidExpression()) return null
    return r
}

fun ParadoxScriptValue.evaluatedValue(strict: Boolean = false, valid: Boolean = false): Any? {
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

fun ParadoxScriptValue.formattedValue(): String {
    val r = when (this) {
        is ParadoxScriptString -> this.value.quoteIfNeeded()
        else -> this.value
    }
    return r
}

// endregion
