package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

val ParadoxScriptVariableName.variableNameId: PsiElement get() = findRequiredChild(VARIABLE_NAME_ID)

val ParadoxScriptPropertyKey.propertyKeyId: PsiElement? get() = findOptionalChild(PROPERTY_KEY_ID)

val ParadoxScriptPropertyKey.quotedPropertyKeyId: PsiElement? get() = findOptionalChild(QUOTED_PROPERTY_KEY_ID)

val ParadoxScriptVariableReference.variableReferenceId: PsiElement get() = findRequiredChild(VARIABLE_REFERENCE_ID)

val IParadoxScriptParameter.parameterId: PsiElement get() = findRequiredChild(PARAMETER_ID)

val IParadoxScriptParameter.defaultValueToken: PsiElement? get() = findOptionalChild(NUMBER_TOKEN)

val ParadoxScriptInlineMathVariableReference.variableReferenceId: PsiElement get() = findRequiredChild(INLINE_MATH_VARIABLE_REFERENCE_ID)

//TODO 应用
fun ParadoxDefinitionProperty.forEachProperty(includeConditional: Boolean = true){
	
}

//TODO 应用
fun ParadoxDefinitionProperty.forEachValue(includeConditional: Boolean = true){
	
}

/**
 * 如果为当前定义属性本身不是定义文件且[propertyName]为空字符串，则直接返回当前定义属性。
 */
fun ParadoxDefinitionProperty.findProperty(propertyName: String, ignoreCase: Boolean = true): ParadoxScriptProperty? {
	if(propertyName.isEmpty()) return this.castOrNull()
	return properties.find { it.name.equals(propertyName, ignoreCase) }
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