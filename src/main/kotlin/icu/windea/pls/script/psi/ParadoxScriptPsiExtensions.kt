@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.linker.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.support.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

/**
 * 遍历当前代码块中的所有（直接作为子节点的）值和属性。
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段的情况。不能嵌套内联。（如，内联脚本）
 */
fun ParadoxScriptBlockElement.processData(
	conditional: Boolean = true,
	inline: Boolean = false,
	processor: (ParadoxScriptMemberElement) -> Boolean
): Boolean {
	val target = this
	return target.processChild {
		when {
			it is ParadoxScriptValue -> doProcessValueChild(it, conditional, inline, processor)
			it is ParadoxScriptProperty -> doProcessPropertyChild(it, conditional, inline, processor)
			conditional && it is ParadoxScriptParameterCondition -> it.processData(processor)
			else -> true
		}
	}
}

/**
 * 遍历当前代码块中的所有（直接作为子节点的）属性。
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段的情况。不能嵌套内联。（如，内联脚本）
 */
fun ParadoxScriptBlockElement.processProperty(
	conditional: Boolean = true,
	inline: Boolean = false,
	processor: (ParadoxScriptProperty) -> Boolean
): Boolean {
	val target = this
	return target.processChild {
		when {
			it is ParadoxScriptProperty -> doProcessPropertyChild(it, conditional, inline, processor)
			conditional && it is ParadoxScriptParameterCondition -> it.processProperty(processor)
			else -> true
		}
	}
}

/**
 * 遍历当前代码块中的所有（直接作为子节点的）值。
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的值。
 * @param inline 是否处理需要内联脚本片段的情况。不能嵌套内联。（如，内联脚本）
 */
fun ParadoxScriptBlockElement.processValue(
	conditional: Boolean = true,
	inline: Boolean = false,
	processor: (ParadoxScriptValue) -> Boolean
): Boolean {
	val target = this
	return target.processChild {
		when {
			it is ParadoxScriptValue -> doProcessValueChild(it, conditional, inline, processor)
			conditional && it is ParadoxScriptParameterCondition -> it.processValue(processor)
			else -> true
		}
	}
}

private fun doProcessValueChild(it: ParadoxScriptValue, conditional: Boolean, inline: Boolean, processor: (ParadoxScriptValue) -> Boolean): Boolean {
	val r = processor(it)
	if(!r) return false
	if(inline) {
		val inlined = ParadoxMemberElementLinker.inlineElement(it)
		if(inlined is ParadoxScriptDefinitionElement) {
			val block = inlined.block
			if(block != null) {
				//不能嵌套内联
				val r1 = block.processValue(conditional, processor = processor)
				if(!r1) return false
			}
		}
		//不处理inlined是value的情况
	}
	return true
}

private fun doProcessPropertyChild(it: ParadoxScriptProperty, conditional: Boolean, inline: Boolean, processor: (ParadoxScriptProperty) -> Boolean): Boolean {
	val r = processor(it)
	if(!r) return false
	if(inline) {
		val inlined = ParadoxMemberElementLinker.inlineElement(it)
		if(inlined is ParadoxScriptDefinitionElement) {
			val block = inlined.block
			if(block != null) {
				//不能嵌套内联
				val r1 = block.processProperty(conditional, processor = processor)
				if(!r1) return false
			}
		}
		//不处理inlined是value的情况
	}
	return true
}


/**
 * 遍历当前参数表达式中的所有（直接作为子节点的）值或属性。
 */
inline fun ParadoxScriptParameterCondition.processData(processor: (ParadoxScriptMemberElement) -> Boolean): Boolean {
	return processChild {
		when {
			it is ParadoxScriptValue -> processor(it)
			it is ParadoxScriptProperty -> processor(it)
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

/**
 * 向上得到第一个定义。
 * 可能为null，可能为自身。
 */
fun PsiElement.findParentDefinition(): ParadoxScriptDefinitionElement? {
	if(language != ParadoxScriptLanguage) return null
	var current: PsiElement = this
	while(current !is PsiFile) {
		if(current is ParadoxScriptDefinitionElement && current.definitionInfo != null) return current
		current = current.parent ?: break
	}
	return null
}

/**
 * 得到指定名字的属性。
 * @param propertyName 要查找到的属性的名字。如果为null，则不指定。如果为空字符串且自身是脚本属性，则返回自身
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段的情况。不能嵌套内联。（如，内联脚本）
 */
fun PsiElement.findProperty(
	propertyName: String? = null,
	ignoreCase: Boolean = true,
	conditional: Boolean = true,
	inline: Boolean = false,
): ParadoxScriptProperty? {
	if(language != ParadoxScriptLanguage) return null
	if(propertyName != null && propertyName.isEmpty()) return this as? ParadoxScriptProperty
	val block = when {
		this is ParadoxScriptDefinitionElement -> this.block
		this is ParadoxScriptBlock -> this
		else -> null
	}
	var result: ParadoxScriptProperty? = null
	block?.processProperty(conditional, inline) {
		if(propertyName == null || propertyName.equals(it.name, ignoreCase)) {
			result = it
			false
		} else {
			true
		}
	}
	return result
}

/**
 * 向上得到第一个属性。可能为null，可能是定义，可能是脚本文件。
 * @param propertyName 要查找到的属性的名字。如果为null，则不指定。如果得到的是脚本文件，则忽略。
 * @param fromParentBlock 是否先向上得到第一个子句，再继续进行查找。
 */
fun PsiElement.findParentProperty(propertyName: String? = null, ignoreCase: Boolean = true, fromParentBlock: Boolean = false): ParadoxScriptDefinitionElement? {
	if(language != ParadoxScriptLanguage) return null
	var current: PsiElement = when {
		fromParentBlock -> this.parentOfType<ParadoxScriptBlockElement>() ?: return null
		this is ParadoxScriptProperty -> this.parent
		else -> this
	}
	while(current !is PsiFile) {
		if(current is ParadoxScriptDefinitionElement) return current.takeIf { propertyName == null || propertyName.equals(it.name, ignoreCase) }
		if(current is ParadoxScriptBlock && !current.isPropertyValue()) return null
		current = current.parent ?: break
	}
	if(current is ParadoxScriptFile) return current
	return null
}


/**
 * 基于路径向下查找指定的属性或值。如果路径为空，则返回查找到的第一个属性或值。
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段的情况。不能嵌套内联。（如，内联脚本）
 * @see ParadoxElementPath
 * @see ParadoxScriptMemberElement
 */
inline fun <reified T : ParadoxScriptMemberElement> ParadoxScriptMemberElement.findByPath(
	path: String = "",
	ignoreCase: Boolean = true,
	conditional: Boolean = true,
	inline: Boolean = false,
): T? {
	if(language != ParadoxScriptLanguage) return null
	var current: ParadoxScriptMemberElement = this
	if(path.isNotEmpty()) {
		val elementPath = ParadoxElementPath.resolve(path)
		val subPathInfos = elementPath.subPathInfos
		for(subPathInfo in subPathInfos) {
			val (subPath) = subPathInfo
			if(subPath == "-") return null //TODO 暂不支持
			current = current.findProperty(subPath, ignoreCase, conditional, inline) ?: return null
		}
	} else {
		current = current.findProperty("", ignoreCase, conditional, inline) ?: return null
	}
	val targetType = T::class.java
	when {
		ParadoxScriptProperty::class.java.isAssignableFrom(targetType) -> {
			return current.castOrNull<ParadoxScriptProperty>() as? T
		}
		ParadoxScriptValue::class.java.isAssignableFrom(targetType) -> {
			return current.castOrNull<ParadoxScriptProperty>()?.findValue() as? T
		}
	}
	return null
}

/**
 * 基于路径向上查找指定的属性（其名字不包括在指定的路径中）。如果路径为空，则返回查找到的第一个属性或值。
 * @param definitionType 如果不为null则要求查找到的属性是定义，如果接着不为空字符串则要求匹配该定义类型表达式。
 * @see ParadoxElementPath
 * @see ParadoxScriptMemberElement
 * @see ParadoxDefinitionTypeExpression
 */
inline fun ParadoxScriptMemberElement.findParentByPath(
	path: String = "",
	ignoreCase: Boolean = true,
	definitionType: String? = null
): ParadoxScriptDefinitionElement? {
	if(language != ParadoxScriptLanguage) return null
	var current: ParadoxScriptMemberElement = this
	if(path.isNotEmpty()) {
		val elementPath = ParadoxElementPath.resolve(path)
		val subPathInfos = elementPath.subPathInfos
		for(subPathInfo in subPathInfos.reversed()) {
			val (subPath) = subPathInfo
			if(subPath == "-") return null //TODO 暂不支持
			current = current.findParentProperty(subPath, ignoreCase) ?: return null
		}
	}
	val result = current.findParentProperty(null) ?: return null
	if(definitionType != null) {
		val definitionInfo = result.definitionInfo ?: return null
		if(definitionType.isNotEmpty()) {
			var match = false
			for(expression in definitionType.split('|')) {
				val (type, subtypes) = ParadoxDefinitionTypeExpression.resolve(expression)
				if(definitionInfo.type == type && (subtypes.isEmpty() || definitionInfo.subtypes.containsAll(subtypes))) {
					match = true
					break
				}
			}
			if(!match) return null
		}
	}
	return result
}


inline fun <reified T : ParadoxScriptValue> ParadoxScriptProperty.findValue(): T? {
	return findChild()
}

inline fun <reified T : ParadoxScriptValue> ParadoxScriptProperty.findBlockValues(): List<T> {
	return findChild<ParadoxScriptBlock>()?.findChildren<T>().orEmpty()
}

inline fun <reified T : ParadoxScriptValue> ParadoxScriptBlockElement.findBlockValues(): List<T> {
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
fun ParadoxScriptExpressionElement.isValidExpression(matchType: Int = CwtConfigMatchType.ALL): Boolean {
	return ParadoxCwtConfigHandler.resolveConfigs(this, orDefault = false, matchType = matchType).size == 1
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