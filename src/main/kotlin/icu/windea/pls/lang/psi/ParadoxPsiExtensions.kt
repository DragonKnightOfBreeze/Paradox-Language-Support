@file:Suppress("unused")

package icu.windea.pls.lang.psi

import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.WalkingContext
import icu.windea.pls.core.collections.WalkingSequence
import icu.windea.pls.core.collections.context
import icu.windea.pls.core.collections.transform
import icu.windea.pls.core.math.MathResult
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.core.toBooleanYesNo
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.manipulation.ParadoxScriptFileManipulationService
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.normalized
import icu.windea.pls.lang.references.ParadoxScriptedVariablePsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationParameterPsiReference
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.evaluators.ParadoxInlineMathExpressionEvaluator
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommandText
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptName
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptColor
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInt
import icu.windea.pls.script.psi.ParadoxScriptLiteralValue
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContext
import icu.windea.pls.script.psi.ParadoxScriptNumberExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.parentBlock
import icu.windea.pls.script.psi.parentProperty
import java.awt.Color
import java.math.BigDecimal

// region PSI Value Resolve Extensions

fun ParadoxScriptExpressionElement.value(resolve: Boolean = true): String? {
    val element = if (resolve) resolved() else this
    if (element == null) return null
    return element.value
}

fun ParadoxScriptExpressionElement.stringValue(resolve: Boolean = true, strict: Boolean = false): String? {
    val element = if (resolve) resolved() else this
    if (element == null) return null
    if (strict && element !is ParadoxScriptStringExpressionElement) return null
    return when (element) {
        is ParadoxScriptStringExpressionElement -> element.value
        is ParadoxScriptLiteralValue -> element.value
        else -> null
    }
}

fun ParadoxScriptExpressionElement.numberValue(resolve: Boolean = true, strict: Boolean = false): BigDecimal? {
    val element = if (resolve) resolved() else this
    if (element == null) return null
    if (strict && element !is ParadoxScriptNumberExpressionElement) return null
    return when (element) {
        is ParadoxScriptNumberExpressionElement -> element.value.toBigDecimalOrNull()
        is ParadoxScriptStringExpressionElement -> element.value.toBigDecimalOrNull()
        else -> null
    }
}

fun ParadoxScriptExpressionElement.booleanValue(resolve: Boolean = true, strict: Boolean = false): Boolean? {
    val element = if (resolve) resolved() else this
    if (element == null) return null
    if (strict && element !is ParadoxScriptBoolean) return null
    return when (element) {
        is ParadoxScriptBoolean -> element.value.toBooleanYesNo()
        else -> true
    }
}

fun ParadoxScriptExpressionElement.intValue(resolve: Boolean = true, strict: Boolean = false): Int? {
    val element = if (resolve) resolved() else this
    if (element == null) return null
    if (strict && element !is ParadoxScriptInt) return null
    return when (element) {
        is ParadoxScriptInt -> element.value.toIntOrNull()
        is ParadoxScriptFloat -> element.value.toFloatOrNull()?.toInt()
        is ParadoxScriptStringExpressionElement -> element.value.toIntOrNull()
        else -> null
    }
}

fun ParadoxScriptExpressionElement.floatValue(resolve: Boolean = true, strict: Boolean = false): Float? {
    val element = if (resolve) resolved() else this
    if (element == null) return null
    if (strict && element !is ParadoxScriptFloat) return null
    return when (element) {
        is ParadoxScriptFloat -> element.value.toFloatOrNull()
        is ParadoxScriptInt -> element.value.toIntOrNull()?.toFloat()
        is ParadoxScriptStringExpressionElement -> element.value.toFloatOrNull()
        else -> null
    }
}

fun ParadoxScriptExpressionElement.colorValue(resolve: Boolean = true): Color? {
    val element = if (resolve) resolved() else this
    if (element == null) return null
    if (element !is ParadoxScriptColor) return null
    return element.color
}

fun ParadoxScriptExpressionElement.inlineMathValue(resolve: Boolean = true): MathResult? {
    val element = if (resolve) resolved() else this
    if (element == null) return null
    if (element !is ParadoxScriptInlineMath) return null
    return ParadoxInlineMathExpressionEvaluator().evaluateOrNull(element)
}

fun ParadoxScriptExpressionElement.formattedValue(resolve: Boolean = true): String? {
    val element = if (resolve) resolved() else this
    if (element == null) return null
    return when (element) {
        is ParadoxScriptStringExpressionElement -> element.value.quoteIfNeeded()
        else -> element.value
    }
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

fun ParadoxLocalisationExpressionElement.isComplexExpression(): Boolean {
    return isCommandExpression() || isDatabaseObjectExpression()
}

fun ParadoxLocalisationExpressionElement.isCommandExpression(): Boolean {
    return this is ParadoxLocalisationCommandText // 简单判断
}

fun ParadoxLocalisationExpressionElement.isDatabaseObjectExpression(strict: Boolean = false): Boolean {
    return this is ParadoxLocalisationConceptName && (!strict || this.textContains(':')) // 简单判断
}

// endregion

// region Sequence Context Builders

/** 如果包含参数化快，是否需要处理其中的子节点。默认为 `false`。 */
var WalkingContext.conditional: Boolean by registerKey(WalkingContext.Keys) { false }

/** @see WalkingContext.conditional */
infix fun WalkingContext.Builder.conditional(value: Boolean? = true) = apply { value?.let { context.conditional = it } }

/** 如果包含内联脚本用法，是否需要先进行内联。默认为 `false`。 */
var WalkingContext.inline: Boolean by registerKey(WalkingContext.Keys) { false }

/** @see WalkingContext.inline */
infix fun WalkingContext.Builder.inline(value: Boolean? = true) = apply { value?.let { context.inline = it } }

// endregion

// region Sequence Builders

/** @see ParadoxScriptFileManipulationService.members */
fun ParadoxScriptMemberContext.members(conditional: Boolean? = null, inline: Boolean? = null): WalkingSequence<ParadoxScriptMember> {
    return ParadoxScriptFileManipulationService.members(this).context { conditional(conditional) + inline(inline) }
}

/** @see ParadoxScriptFileManipulationService.members */
fun ParadoxScriptMemberContext.properties(conditional: Boolean? = null, inline: Boolean? = null): WalkingSequence<ParadoxScriptProperty> {
    return members(conditional, inline).transform { filterIsInstance<ParadoxScriptProperty>() }
}

/** @see ParadoxScriptFileManipulationService.members */
fun ParadoxScriptMemberContext.values(conditional: Boolean? = null, inline: Boolean? = null): WalkingSequence<ParadoxScriptValue> {
    return members(conditional, inline).transform { filterIsInstance<ParadoxScriptValue>() }
}

// endregion
