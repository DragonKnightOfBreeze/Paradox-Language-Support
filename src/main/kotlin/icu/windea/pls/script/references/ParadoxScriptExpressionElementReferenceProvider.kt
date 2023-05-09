package icu.windea.pls.script.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxScriptExpressionElementReferenceProvider : PsiReferenceProvider() {
	override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
		//尝试兼容可能包含参数的情况
		//if(text.isParameterized()) return PsiReference.EMPTY_ARRAY
		
		val isKey = element is ParadoxScriptPropertyKey
		
		//尝试解析为复杂枚举值声明
		if(element is ParadoxScriptStringExpressionElement) {
			val complexEnumValueInfo = ParadoxComplexEnumValueHandler.getInfo(element)
			if(complexEnumValueInfo != null) {
				val project = element.project
				val config = complexEnumValueInfo.getConfig(project)
				if(config != null) {
					val text = element.text
					val textRange = TextRange.create(0, text.length).unquote(text) //unquoted text
					val reference = ParadoxComplexEnumValuePsiReference(element, textRange, complexEnumValueInfo, project)
					return arrayOf(reference)
				}
			}
		}
		
		//尝试基于规则进行解析
		val configs = ParadoxConfigHandler.getConfigs(element, !isKey, isKey)
		val config = configs.firstOrNull()
		if(config != null) {
			val configGroup = config.info.configGroup
			val configExpression = config.expression
			when {
				configExpression.type.isValueSetValueType() -> {
					if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
					val text = element.text
					val textRange = getTextRange(element, text)
					//quoted -> only value set value name, no scope info
					if(text.isLeftQuoted()) {
						val reference = ParadoxScriptExpressionPsiReference(element, textRange, config, isKey)
						return arrayOf(reference)
					}
					val valueFieldExpression = ParadoxValueSetValueExpression.resolve(text, textRange, config, configGroup, isKey)
					if(valueFieldExpression == null) return PsiReference.EMPTY_ARRAY
					return valueFieldExpression.getReferences(element)
				}
				configExpression.type.isScopeFieldType() -> {
					if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
					val text = element.text
					if(text.isLeftQuoted()) return PsiReference.EMPTY_ARRAY
					val textRange = getTextRange(element, text)
					val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup, isKey)
					if(scopeFieldExpression == null) return PsiReference.EMPTY_ARRAY
					return scopeFieldExpression.getReferences(element)
				}
				configExpression.type.isValueFieldType() -> {
					if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
					val text = element.text
					if(text.isLeftQuoted()) return PsiReference.EMPTY_ARRAY
					val textRange = getTextRange(element, text)
					val valueFieldExpression = ParadoxValueFieldExpression.resolve(text, textRange, configGroup, isKey)
					if(valueFieldExpression == null) return PsiReference.EMPTY_ARRAY
					return valueFieldExpression.getReferences(element)
				}
				configExpression.type.isVariableFieldType() -> {
					if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
					val text = element.text
					if(text.isLeftQuoted()) return PsiReference.EMPTY_ARRAY
					val textRange = getTextRange(element, text)
					val variableFieldExpression = ParadoxVariableFieldExpression.resolve(text, textRange, configGroup, isKey)
					if(variableFieldExpression == null) return PsiReference.EMPTY_ARRAY
					return variableFieldExpression.getReferences(element)
				}
				else -> {
					if(element !is ParadoxScriptExpressionElement) return PsiReference.EMPTY_ARRAY
					val text = element.text
					val textRange = getTextRange(element, text)
					val reference = ParadoxScriptExpressionPsiReference(element, textRange, config, isKey)
					return arrayOf(reference)
				}
			}
		}
		return PsiReference.EMPTY_ARRAY
	}
	
	private fun getTextRange(element: PsiElement, text: String): TextRange {
		return when {
			element is ParadoxScriptBlock -> TextRange.create(0, 1) //left curly brace
			else -> TextRange.create(0, text.length).unquote(text) //unquoted text
		}
	}
}
