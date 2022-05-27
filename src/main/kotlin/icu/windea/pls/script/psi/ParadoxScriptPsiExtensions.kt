package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

val ParadoxScriptVariableName.variableNameId: PsiElement get() = findRequiredChild(VARIABLE_NAME_ID)

val ParadoxScriptPropertyKey.propertyKeyId: PsiElement? get() = findOptionalChild(PROPERTY_KEY_ID)

val ParadoxScriptPropertyKey.quotedPropertyKeyId: PsiElement? get() = findOptionalChild(QUOTED_PROPERTY_KEY_ID)

val ParadoxScriptVariableReference.variableReferenceId: PsiElement get() = findRequiredChild(VARIABLE_REFERENCE_ID)

val ParadoxScriptParameter.parameterId: PsiElement get() = findRequiredChild(PARAMETER_ID)

val ParadoxScriptParameter.defaultValueToken: PsiElement? get() = findOptionalChild(NUMBER_TOKEN)

val ParadoxScriptParameterConditionParameter.parameterId: PsiElement get() = findRequiredChild(PARAMETER_ID)

val ParadoxScriptInlineMathVariableReference.variableReferenceId: PsiElement get() = findRequiredChild(INLINE_MATH_VARIABLE_REFERENCE_ID)

val ParadoxScriptInlineMathParameter.parameterId: PsiElement get() = findRequiredChild(PARAMETER_ID)

val ParadoxScriptInlineMathParameter.defaultValueToken: PsiElement? get() = findOptionalChild(NUMBER_TOKEN)


/**
 * 遍历当前代码块中的所有（直接作为子节点的）属性。
 * @param includeConditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 */
inline fun ParadoxScriptBlock.processProperties(includeConditional: Boolean = false, processor: (ParadoxScriptProperty) -> Boolean): Boolean {
	return processChildren {
		when {
			it is ParadoxScriptProperty -> processor(it)
			includeConditional && it is ParadoxScriptParameterCondition -> it.processProperties(processor)
			else -> true
		}
	}
}

/**
 * 遍历当前代码块中的所有（直接作为子节点的）值。
 * @param includeConditional 是否也包括间接作为其中的参数表达式的子节点的值。
 */
inline fun ParadoxScriptBlock.processValues(includeConditional: Boolean = false, processor: (ParadoxScriptValue) -> Boolean): Boolean {
	return processChildren {
		when {
			it is ParadoxScriptValue -> processor(it)
			includeConditional && it is ParadoxScriptParameterCondition -> it.processValues(processor)
			else -> true
		}
	}
}

/**
 * 遍历当前参数表达式中的所有（直接作为子节点的）属性。
 */
inline fun ParadoxScriptParameterCondition.processProperties(processor: (ParadoxScriptProperty) -> Boolean): Boolean {
	return processChildren {
		when {
			it is ParadoxScriptProperty -> processor(it)
			else -> true
		}
	}
}

/**
 * 遍历当前参数表达式中的所有（直接作为子节点的）值。
 */
inline fun ParadoxScriptParameterCondition.processValues(processor: (ParadoxScriptValue) -> Boolean): Boolean {
	return processChildren {
		when {
			it is ParadoxScriptValue -> processor(it)
			else -> true
		}
	}
}


inline fun <reified T : ParadoxScriptValue> ParadoxScriptProperty.findPropertyValue(): T? {
	return findOptionalChild<ParadoxScriptPropertyValue>()?.findOptionalChild()
}

inline fun <reified T : ParadoxScriptValue> ParadoxScriptBlock.findValues(): List<T> {
	return filterChildOfType() 
}

/**
 * 得到指定名字的definitionProperty。如果为当前定义属性本身不是定义文件且[propertyName]为空字符串，则直接返回当前定义属性。
 */
fun ParadoxDefinitionProperty.findProperty(propertyName: String, ignoreCase: Boolean = true): ParadoxScriptProperty? {
	if(propertyName.isEmpty() && this is ParadoxScriptProperty) return this
	block?.processProperties(includeConditional = true) {
		if(it.name.equals(propertyName, ignoreCase)) return it
		true
	}
	return null
}

/**
 * 得到上一级definition，可能为自身，可能为null。
 */
fun PsiElement.findParentDefinition(skipThis: Boolean = false): ParadoxDefinitionProperty? {
	if(language != ParadoxScriptLanguage) return null
	var current: PsiElement = (if(skipThis) this.parent else this) ?: return null
	while(current !is PsiFile) {
		if(current is ParadoxDefinitionProperty && current.definitionInfo != null) return current
		current = current.parent ?: break
	}
	return null
}

/**
 * 得到上一级definitionProperty，可能为自身，可能为null，可能也是definition。
 */
fun PsiElement.findParentDefinitionProperty(skipThis: Boolean = false): ParadoxDefinitionProperty? {
	if(language != ParadoxScriptLanguage) return null
	var current: PsiElement = (if(skipThis) this.parent else this) ?: return null
	while(current !is PsiFile) {
		if(current is ParadoxDefinitionProperty) return current
		current = current.parent ?: break
	}
	return null
}