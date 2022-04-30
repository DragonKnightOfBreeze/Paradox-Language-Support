package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

val ParadoxScriptVariableName.variableNameId: PsiElement get() = findRequiredChild(VARIABLE_NAME_ID)

val ParadoxScriptPropertyKey.propertyKeyId: PsiElement? get() = findChild(PROPERTY_KEY_ID)

val ParadoxScriptPropertyKey.quotedPropertyKeyId: PsiElement? get() = findChild(QUOTED_PROPERTY_KEY_ID)

val ParadoxScriptVariableReference.variableReferenceId: PsiElement get() = findRequiredChild(VARIABLE_REFERENCE_ID)

fun ParadoxDefinitionProperty.findProperty(propertyName: String, ignoreCase: Boolean = true): ParadoxScriptProperty? {
	return properties.find { it.name.equals(propertyName, ignoreCase) }
}

//fun ParadoxDefinitionProperty.findProperties(propertyName: String, ignoreCase: Boolean = false): List<ParadoxScriptProperty> {
//	return properties.filter { it.name.equals(propertyName, ignoreCase) }
//}
//
//fun ParadoxDefinitionProperty.findValue(value: String, ignoreCase: Boolean = false): ParadoxScriptValue? {
//	return values.find { it.value.equals(value, ignoreCase) }
//}
//
//fun ParadoxDefinitionProperty.findValues(value: String, ignoreCase: Boolean = false): List<ParadoxScriptValue> {
//	return values.filter { it.value.equals(value, ignoreCase) }
//}
//
//fun ParadoxScriptBlock.findProperty(propertyName: String, ignoreCase: Boolean = false): ParadoxScriptProperty? {
//	return propertyList.find { it.name.equals(propertyName, ignoreCase) }
//}
//
//fun ParadoxScriptBlock.findProperties(propertyName: String, ignoreCase: Boolean = false): List<ParadoxScriptProperty> {
//	return propertyList.filter { it.name.equals(propertyName, ignoreCase) }
//}
//
//fun ParadoxScriptBlock.findValue(value: String, ignoreCase: Boolean = false): ParadoxScriptValue? {
//	return valueList.find { it.value.equals(value, ignoreCase) }
//}
//
//fun ParadoxScriptBlock.findValues(value: String, ignoreCase: Boolean = false): List<ParadoxScriptValue> {
//	return valueList.filter { it.value.equals(value, ignoreCase) }
//}


/**
 * 得到上一级definitionProperty，可能为自身，可能为null，可能也是definition。
 */
fun PsiElement.findParentDefinitionProperty(): ParadoxDefinitionProperty? {
	var current: PsiElement = this
	do {
		if(current is ParadoxDefinitionProperty) {
			return current
		}
		current = current.parent ?: break
	} while(current !is PsiFile)
	return null
}

/**
 * 得到上一级definitionProperty，跳过正在填写的，可能为自身，可能为null，可能也是definition。
 */
fun PsiElement.findParentDefinitionPropertySkipThis(): ParadoxDefinitionProperty? {
	var current: PsiElement = this
	do {
		if(current is ParadoxScriptRootBlock) {
			return (current.parent ?: break) as ParadoxDefinitionProperty
		} else if(current is ParadoxScriptBlock) {
			return (current.parent.parent ?: break) as ParadoxDefinitionProperty
		}
		current = current.parent ?: break
	} while(current !is PsiFile)
	return null
}