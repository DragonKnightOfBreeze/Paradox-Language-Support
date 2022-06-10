package icu.windea.pls.script.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

val ParadoxScriptVariableName.variableNameId: PsiElement get() = findRequiredChild(VARIABLE_NAME_ID)

val ParadoxScriptPropertyKey.propertyKeyId: PsiElement? get() = findOptionalChild(PROPERTY_KEY_TOKEN)

val ParadoxScriptPropertyKey.quotedPropertyKeyId: PsiElement? get() = findOptionalChild(QUOTED_PROPERTY_KEY_TOKEN)

val ParadoxScriptVariableReference.variableReferenceId: PsiElement get() = findRequiredChild(VARIABLE_REFERENCE_ID)

val ParadoxScriptParameterConditionParameter.parameterId: PsiElement get() = findRequiredChild(INPUT_PARAMETER_ID)

val ParadoxScriptInlineMathVariableReference.variableReferenceId: PsiElement get() = findRequiredChild(INLINE_MATH_VARIABLE_REFERENCE_ID)

val IParadoxScriptParameter.parameterId: PsiElement? get() = findOptionalChild(PARAMETER_ID)

val IParadoxScriptParameter.defaultValueToken: PsiElement? get() = findOptionalChild(NUMBER_TOKEN)

internal fun ParadoxDefinitionProperty.clearDefinitionElementInfo() {
	this.accept(object : PsiRecursiveElementVisitor() {
		override fun visitElement(element: PsiElement) {
			if(element is ParadoxScriptProperty || element is ParadoxScriptValue || element is ParadoxScriptPropertyValue) {
				if(element is ParadoxScriptProperty || element is ParadoxScriptValue) {
					element.putUserData(PlsKeys.cachedParadoxDefinitionElementInfoKey, null)
				}
				super.visitElement(element)
			}
		}
	})
}

/**
 * 遍历当前代码块中的所有（直接作为子节点的）属性。
 * @param includeConditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 */
inline fun IParadoxScriptBlock.processProperty(includeConditional: Boolean = false, processor: (ParadoxScriptProperty) -> Boolean): Boolean {
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
inline fun IParadoxScriptBlock.processValue(includeConditional: Boolean = false, processor: (ParadoxScriptValue) -> Boolean): Boolean {
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
	block?.processProperty(includeConditional = true) {
		if(it.name.equals(propertyName, ignoreCase)) return it
		true
	}
	return null
}

/**
 * 得到上一级definition，可能为null。
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
 * 得到上一级definitionProperty，可能为null，可能也是definition。
 */
fun PsiElement.findParentDefinitionProperty(fromParentBlock: Boolean = false): ParadoxDefinitionProperty? {
	if(language != ParadoxScriptLanguage) return null
	var current: PsiElement = if(fromParentBlock) this.parent else this
	while(current !is PsiFile) {
		if(fromParentBlock) {
			if(current is ParadoxScriptRootBlock) {
				return (current.parent ?: break) as ParadoxDefinitionProperty
			} else if(current is ParadoxScriptBlock) {
				return (current.parent.parent ?: break) as ParadoxDefinitionProperty
			}
		} else {
			if(current is ParadoxDefinitionProperty) return current
		}
		current = current.parent ?: break
	}
	return null
}

fun ParadoxScriptPropertyKey.isSimpleExpression():Boolean{
	val singleChild = this.firstChild?.takeIf { it.nextSibling == null } ?: return false
	return singleChild.elementType.let { it == PROPERTY_KEY_TOKEN || it == ParadoxScriptElementTypes.QUOTED_PROPERTY_KEY_TOKEN }
}

fun ParadoxScriptString.isSimpleExpression():Boolean{
	val singleChild = this.firstChild?.takeIf { it.nextSibling == null } ?: return false
	return singleChild.elementType.let { it == STRING_TOKEN || it == ParadoxScriptElementTypes.QUOTED_STRING_TOKEN }
}

/**
 * 判断keyExpression是否可能是linkExpression。（如：`root`、`root.owner`）
 */
fun ParadoxScriptPropertyKey.mayBeLinkExpression(): Boolean{
	val propertyConfig = this.getPropertyConfig()  ?: return false
	return CwtConfigHandler.supportsScopes(propertyConfig)
}
