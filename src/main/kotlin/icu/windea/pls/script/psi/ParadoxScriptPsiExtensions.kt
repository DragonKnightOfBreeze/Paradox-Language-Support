@file:Suppress("unused")

package icu.windea.pls.script.psi

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.references.script.*
import icu.windea.pls.lang.util.*
import java.awt.*

inline fun <reified T : ParadoxScriptValue> ParadoxScriptProperty.propertyValue(): T? {
    return findChild<T>(forward = false)
}

inline fun <reified T : ParadoxScriptValue> ParadoxScriptProperty.valueList(): List<T> {
    return findChild<ParadoxScriptBlock>()?.findChildren<T>().orEmpty()
}

inline fun <reified T : ParadoxScriptValue> ParadoxScriptBlockElement.valueList(): List<T> {
    return findChildren<T>()
}

fun ParadoxScriptMemberElement.isBlockMember(): Boolean {
    return parent.let { it is ParadoxScriptBlockElement || it is ParadoxScriptParameterCondition }
}

fun ParadoxScriptValue.isScriptedVariableValue(): Boolean {
    return parent is ParadoxScriptScriptedVariable
}

fun ParadoxScriptValue.isPropertyValue(): Boolean {
    return parent is ParadoxScriptProperty
}

fun ParadoxScriptExpressionElement.isExpression(): Boolean {
    return when {
        this is ParadoxScriptPropertyKey -> true
        this is ParadoxScriptValue && parent.let { it is ParadoxScriptProperty || it is ParadoxScriptBlockElement || it is ParadoxScriptParameterCondition } -> true
        else -> false
    }
}

/**
 * 判断当前字符串表达式是否在顶层或者子句中或者作为属性的值，并且拥有唯一匹配的CWT规则。
 */
fun ParadoxScriptExpressionElement.isValidExpression(matchOptions: Int = ParadoxExpressionMatcher.Options.Default): Boolean {
    return ParadoxExpressionManager.getConfigs(this, orDefault = false, matchOptions = matchOptions).size == 1
}

fun ParadoxScriptExpressionElement.isResolvableExpression(): Boolean {
    return this is ParadoxScriptStringExpressionElement || this is ParadoxScriptInt || this is ParadoxScriptFloat
}

fun ParadoxScriptExpressionElement.isDefinitionRootKeyOrName(): Boolean {
    return when {
        this is ParadoxScriptPropertyKey -> isDefinitionRootKey()
        this is ParadoxScriptValue -> isDefinitionName()
        else -> false
    }
}

fun ParadoxScriptPropertyKey.isDefinitionRootKey(): Boolean {
    val definition = this.parent?.castOrNull<ParadoxScriptProperty>() ?: return false
    if (definition.definitionInfo != null) return true
    return false
}

fun ParadoxScriptValue.isDefinitionName(): Boolean {
    //#131
    if (!this.isResolvableExpression()) return false

    val nameProperty = this.parent?.castOrNull<ParadoxScriptProperty>() ?: return false
    //def = def_name
    if (nameProperty.definitionInfo.let { it != null && it.typeConfig.nameField == "" }) return true
    val block = nameProperty.parent?.castOrNull<ParadoxScriptBlock>() ?: return false
    val definition = block.parent?.castOrNull<ParadoxScriptProperty>() ?: return false
    //def = { name_prop = def_name }
    if (definition.definitionInfo.let { it != null && it.typeConfig.nameField == nameProperty.name }) return true
    return false
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
        is ParadoxScriptScriptedVariableReference -> this.resolved()?.scriptedVariableValue?.resolveValue()
        is ParadoxScriptBlock -> null //unsupported
        else -> null //unsupported
    }
}

fun ParadoxScriptExpressionElement.resolved(): ParadoxScriptExpressionElement? {
    return when (this) {
        is ParadoxScriptScriptedVariableReference -> this.resolved()?.scriptedVariableValue
        else -> this
    }
}

fun ParadoxScriptExpressionElement.stringText(valid: Boolean = false): String? {
    if (valid && !this.isValidExpression()) return null
    val resolved = this.resolved() ?: return null
    return when (resolved) {
        is ParadoxScriptPropertyKey -> resolved.text
        is ParadoxScriptString -> resolved.text
        is ParadoxScriptInt -> resolved.value
        is ParadoxScriptFloat -> resolved.value
        else -> null
    }
}

fun ParadoxScriptValue.tagType(): CwtTagType? {
    if (this !is ParadoxScriptString) return null
    if (!this.isBlockMember()) return null
    val references = references
    run {
        val tagReference = references.firstNotNullOfOrNull { it.castOrNull<ParadoxScriptTagAwarePsiReference>() }
        if (tagReference == null) return@run
        return tagReference.config.tagType
    }
    run {
        val expressionReference = references.firstNotNullOfOrNull { it.castOrNull<ParadoxScriptExpressionPsiReference>() }
        if (expressionReference == null) return@run
        return expressionReference.config.castOrNull<CwtValueConfig>()?.tagType
    }
    return null
}
