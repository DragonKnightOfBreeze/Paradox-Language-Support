@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import java.awt.*

inline fun <reified T : ParadoxScriptValue> ParadoxScriptProperty.value(): T? {
	return findChild()
}

inline fun <reified T : ParadoxScriptValue> ParadoxScriptProperty.blockValues(): List<T> {
	return findChild<ParadoxScriptBlock>()?.findChildren<T>().orEmpty()
}

inline fun <reified T : ParadoxScriptValue> ParadoxScriptBlockElement.blockValues(): List<T> {
	return findChildren()
}


fun ParadoxScriptValue.isScriptedVariableValue(): Boolean {
	return parent is ParadoxScriptScriptedVariable
}

fun ParadoxScriptValue.isPropertyValue(): Boolean {
	return parent is ParadoxScriptProperty
}

fun ParadoxScriptValue.isBlockValue(): Boolean {
	return parent is ParadoxScriptBlockElement
}

fun ParadoxScriptExpressionElement.isExpression(): Boolean {
	return when {
		this is ParadoxScriptPropertyKey -> true
		this is ParadoxScriptValue && (this.isPropertyValue() || this.isBlockValue()) -> true
		else -> false
	}
}

fun ParadoxScriptStringExpressionElement.isParameterAwareExpression(): Boolean {
	return !this.text.isLeftQuoted() && this.textContains('$')
}

/**
 * 判断当前字符串表达式是否在顶层或者子句中或者作为属性的值，并且拥有唯一匹配的CWT规则。
 */
fun ParadoxScriptExpressionElement.isValidExpression(matchType: Int = CwtConfigMatchType.DEFAULT): Boolean {
	return ParadoxCwtConfigHandler.getConfigs(this, orDefault = false, matchType = matchType).size == 1
}

fun ASTNode.isParameterAwareExpression(): Boolean {
	return !this.processChild { it.elementType != PARAMETER }
}

fun String.isParameterAwareExpression(): Boolean {
	return !this.isLeftQuoted() && this.any { it == '$' }
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


fun ParadoxScriptValue.booleanValue(): Boolean? {
	val resolved = this.resolved() ?: return null
	return when(resolved) {
		is ParadoxScriptBoolean -> resolved.booleanValue
		else -> null
	}
}

fun ParadoxScriptExpressionElement.intValue(): Int? {
	val resolved = this.resolved() ?: return null
	return when(resolved) {
		is ParadoxScriptPropertyKey -> resolved.value.toIntOrNull()
		is ParadoxScriptInt -> resolved.intValue
		is ParadoxScriptString -> resolved.value.toIntOrNull()
		else -> null
	}
}

fun ParadoxScriptExpressionElement.floatValue(): Float? {
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

fun ParadoxScriptValue.colorValue(): Color? {
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