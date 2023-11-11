package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import java.awt.*

inline fun <reified T : ParadoxScriptValue> ParadoxScriptProperty.propertyValue(): T? {
    return findChild()
}

inline fun <reified T : ParadoxScriptValue> ParadoxScriptProperty.valueList(): List<T> {
    return findChild<ParadoxScriptBlock>()?.findChildren<T>().orEmpty()
}

inline fun <reified T : ParadoxScriptValue> ParadoxScriptBlockElement.valueList(): List<T> {
    return findChildren()
}


fun ParadoxScriptValue.isScriptedVariableValue(): Boolean {
    return parent is ParadoxScriptScriptedVariable
}

fun ParadoxScriptValue.isPropertyValue(): Boolean {
    return parent is ParadoxScriptProperty
}

fun ParadoxScriptValue.isBlockValue(): Boolean {
    return parent.let { it is ParadoxScriptBlockElement || it is ParadoxScriptParameterCondition }
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
fun ParadoxScriptExpressionElement.isValidExpression(matchOptions: Int = CwtConfigMatcher.Options.Default): Boolean {
    return CwtConfigHandler.getConfigs(this, orDefault = false, matchOptions = matchOptions).size == 1
}

fun PsiElement.isExpressionOrMemberContext(): Boolean {
    return this is ParadoxScriptDefinitionElement || this is ParadoxScriptBlockElement || this is ParadoxScriptParameterCondition
}

fun ParadoxScriptStringExpressionElement.isDefinitionRootKeyOrName(): Boolean {
    return when {
        this is ParadoxScriptPropertyKey -> isDefinitionRootKey()
        this is ParadoxScriptString -> isDefinitionName()
        else -> false
    }
}

fun ParadoxScriptPropertyKey.isDefinitionRootKey(): Boolean {
    val definition = this.parent?.castOrNull<ParadoxScriptProperty>() ?: return false
    if(definition.definitionInfo != null) return true
    return false
}

fun ParadoxScriptString.isDefinitionName(): Boolean {
    val nameProperty = this.parent?.castOrNull<ParadoxScriptProperty>() ?: return false
    //def = def_name
    if(nameProperty.definitionInfo.let { it != null && it.typeConfig.nameField == "" }) return true
    val block = nameProperty.parent?.castOrNull<ParadoxScriptBlock>() ?: return false
    val definition = block.parent?.castOrNull<ParadoxScriptProperty>() ?: return false
    //def = { name_prop = def_name }
    if(definition.definitionInfo.let { it != null && it.typeConfig.nameField == nameProperty.name }) return true
    return false
}


fun ParadoxScriptExpressionElement.value(valid: Boolean = false): String? {
    if(valid && !this.isValidExpression()) return null
    val resolved = this.resolved() ?: return null
    return resolved.value
}

fun ParadoxScriptValue.booleanValue(valid: Boolean = false): Boolean? {
    if(valid && !this.isValidExpression()) return null
    val resolved = this.resolved() ?: return null
    return when(resolved) {
        is ParadoxScriptBoolean -> resolved.booleanValue
        else -> null
    }
}

fun ParadoxScriptExpressionElement.intValue(valid: Boolean = false): Int? {
    if(valid && !this.isValidExpression()) return null
    val resolved = this.resolved() ?: return null
    return when(resolved) {
        is ParadoxScriptPropertyKey -> resolved.value.toIntOrNull()
        is ParadoxScriptInt -> resolved.intValue
        is ParadoxScriptString -> resolved.value.toIntOrNull()
        else -> null
    }
}

fun ParadoxScriptExpressionElement.floatValue(valid: Boolean = false): Float? {
    if(valid && !this.isValidExpression()) return null
    val resolved = this.resolved() ?: return null
    return when(resolved) {
        is ParadoxScriptPropertyKey -> resolved.value.toFloatOrNull()
        is ParadoxScriptFloat -> resolved.floatValue
        is ParadoxScriptString -> resolved.value.toFloatOrNull()
        else -> null
    }
}

fun ParadoxScriptExpressionElement.stringText(valid: Boolean = false): String? {
    if(valid && !this.isValidExpression()) return null
    val resolved = this.resolved() ?: return null
    return when(resolved) {
        is ParadoxScriptPropertyKey -> resolved.text
        is ParadoxScriptString -> resolved.text
        is ParadoxScriptInt -> resolved.value
        is ParadoxScriptFloat -> resolved.value
        else -> null
    }
}

fun ParadoxScriptExpressionElement.stringValue(valid: Boolean = false): String? {
    if(valid && !this.isValidExpression()) return null
    val resolved = this.resolved() ?: return null
    return when(resolved) {
        is ParadoxScriptPropertyKey -> resolved.value
        is ParadoxScriptString -> resolved.value
        is ParadoxScriptInt -> resolved.value
        is ParadoxScriptFloat -> resolved.value
        else -> null
    }
}

fun ParadoxScriptValue.colorValue(valid: Boolean = false): Color? {
    if(valid && !this.isValidExpression()) return null
    return when(this) {
        is ParadoxScriptColor -> this.color
        else -> null
    }
}

fun ParadoxScriptExpressionElement.resolved(): ParadoxScriptExpressionElement? {
    return when(this) {
        is ParadoxScriptScriptedVariableReference -> this.referenceValue
        else -> this
    }
}
