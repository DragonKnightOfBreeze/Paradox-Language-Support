package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

/**
 * 遍历当前代码块中的所有（直接作为子节点的）属性。
 * @param includeConditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 */
inline fun ParadoxScriptBlockElement.processProperty(includeConditional: Boolean = false, processor: (ParadoxScriptProperty) -> Boolean): Boolean {
	return processChild {
		when {
			it is ParadoxScriptProperty -> processor(it)
			includeConditional && it is ParadoxScriptParameterCondition -> it.processProperty(processor)
			else -> true
		}
	}
}

/**
 * 遍历当前代码块中的所有（直接作为子节点的）值。
 * @param includeConditional 是否也包括间接作为其中的参数表达式的子节点的值。
 */
inline fun ParadoxScriptBlockElement.processValue(includeConditional: Boolean = false, processor: (ParadoxScriptValue) -> Boolean): Boolean {
	return processChild {
		when {
			it is ParadoxScriptValue -> processor(it)
			includeConditional && it is ParadoxScriptParameterCondition -> it.processValue(processor)
			else -> true
		}
	}
}

/**
 * 遍历当前参数表达式中的所有（直接作为子节点的）属性。
 */
inline fun ParadoxScriptParameterCondition.processProperty(processor: (ParadoxScriptProperty) -> Boolean): Boolean {
	return processChild {
		when {
			it is ParadoxScriptProperty -> processor(it)
			else -> true
		}
	}
}

/**
 * 遍历当前参数表达式中的所有（直接作为子节点的）值。
 */
inline fun ParadoxScriptParameterCondition.processValue(processor: (ParadoxScriptValue) -> Boolean): Boolean {
	return processChild {
		when {
			it is ParadoxScriptValue -> processor(it)
			else -> true
		}
	}
}

inline fun <reified T : ParadoxScriptValue> ParadoxScriptProperty.findValue(): T? {
	return findChild()
}

inline fun <reified T : ParadoxScriptValue> ParadoxScriptProperty.findBlockValues(): List<T> {
	return findChild<ParadoxScriptBlock>()?.findChildren<T>().orEmpty()
}

inline fun <reified T : ParadoxScriptValue> ParadoxScriptBlockElement.findValues(): List<T> {
	return findChildren()
}

/**
 * 向上得到第一个定义。
 * 可能为null，可能为自身。
 */
fun PsiElement.findParentDefinition(): ParadoxDefinitionProperty? {
	if(language != ParadoxScriptLanguage) return null
	var current: PsiElement = this
	while(current !is PsiFile) {
		if(current is ParadoxDefinitionProperty && current.definitionInfo != null) return current
		current = current.parent ?: break
	}
	return null
}

/**
 * 得到指定名字的属性。
 * 如果为当前定义属性（非文件），且[propertyName]为空字符串，则直接返回自身。
 */
fun ParadoxDefinitionProperty.findProperty(propertyName: String, ignoreCase: Boolean = true): ParadoxScriptProperty? {
	if(propertyName.isEmpty() && this is ParadoxScriptProperty) return this
	block?.processProperty(includeConditional = true) {
		if(it.name.equals(propertyName, ignoreCase)) return it
		true
	}
	return null
}

/**
 * 向上得到第一个属性。
 * 可能为null，可能是定义。
 * @param fromParentBlock 是否先向上得到第一个子句，再继续进行查找。
 */
fun PsiElement.findParentProperty(fromParentBlock: Boolean = false): ParadoxDefinitionProperty? {
	if(language != ParadoxScriptLanguage) return null
	var current: PsiElement = if(fromParentBlock) this.parent else this
	while(current !is PsiFile) {
		if(fromParentBlock) {
			if(current is ParadoxScriptBlockElement) {
				return current.parent as? ParadoxDefinitionProperty ?: break
			}
		} else {
			if(current is ParadoxDefinitionProperty) return current
		}
		current = current.parent ?: break
	}
	return null
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


fun ParadoxScriptValue.isScriptedVariableValue(): Boolean {
	return parent is ParadoxScriptScriptedVariable
}

fun ParadoxScriptValue.isPropertyValue(): Boolean {
	return parent is ParadoxScriptProperty
}

fun ParadoxScriptValue.isBlockValue(): Boolean {
	return parent is ParadoxScriptBlockElement
}

fun PsiElement.isPropertyOrBLockValue(): Boolean {
	return when {
		this is ParadoxScriptProperty -> true
		this is ParadoxScriptValue && (this.isPropertyValue() || this.isBlockValue()) -> true
		else -> false
	}
}

fun PsiElement.isExpression(): Boolean {
	return when {
		this is ParadoxScriptPropertyKey -> true
		this is ParadoxScriptValue && (this.isPropertyValue() || this.isBlockValue()) -> true
		else -> false
	}
}

fun ParadoxScriptStringExpressionElement.isParameterAwareExpression(): Boolean {
	return !this.text.isLeftQuoted() && this.textContains('$')
}

fun ASTNode.isParameterAwareExpression(): Boolean {
	return !this.processChild { it.elementType != PARAMETER }
}

fun String.isParameterAwareExpression(): Boolean {
	return !this.isLeftQuoted() && this.any { it == '$' }
}

