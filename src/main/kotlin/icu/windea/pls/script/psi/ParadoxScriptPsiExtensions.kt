package icu.windea.pls.script.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

fun PsiElement.isParadoxScriptPsiElement() : Boolean{
	val elementType = this.elementType?:return false
	return elementType is ParadoxScriptTokenType || elementType is ParadoxScriptElementType
}

val ParadoxScriptVariableName.variableNameId: PsiElement get() = findRequiredChild(VARIABLE_NAME_ID)

val ParadoxScriptPropertyKey.propertyKeyId: PsiElement? get() = findChild(PROPERTY_KEY_ID)

val ParadoxScriptPropertyKey.quotedPropertyKeyId: PsiElement? get() = findChild(QUOTED_PROPERTY_KEY_ID)

val ParadoxScriptVariableReference.variableReferenceId: PsiElement get() = findRequiredChild(VARIABLE_REFERENCE_ID)

/**
 * 如果为当前定义属性本身不是定义文件且[propertyName]为空字符串，则直接返回当前定义属性。
 */
fun ParadoxDefinitionProperty.findProperty(propertyName: String, ignoreCase: Boolean = true): ParadoxScriptProperty? {
	if(propertyName.isEmpty()) return this.castOrNull()
	return properties.find { it.name.equals(propertyName, ignoreCase) }
}

/**
 * 如果为当前定义属性本身不是定义文件且[propertyName]为空字符串，则直接返回当前定义属性组成的单例列表。
 */
fun ParadoxDefinitionProperty.findProperties(propertyName: String, ignoreCase: Boolean = false): List<ParadoxScriptProperty> {
	if(propertyName.isEmpty()) return this.castOrNull<ParadoxScriptProperty>().toSingletonListOrEmpty()
	return properties.filter { it.name.equals(propertyName, ignoreCase) }
}

//fun ParadoxDefinitionProperty.findValue(value: String, ignoreCase: Boolean = false): ParadoxScriptValue? {
//	return values.find { it.value.equals(value, ignoreCase) }
//}
//
//fun ParadoxDefinitionProperty.findValues(value: String, ignoreCase: Boolean = false): List<ParadoxScriptValue> {
//	return values.filter { it.value.equals(value, ignoreCase) }
//}

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