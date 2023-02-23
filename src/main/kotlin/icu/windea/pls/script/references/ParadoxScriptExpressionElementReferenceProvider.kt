package icu.windea.pls.script.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.search.selectors.*
import icu.windea.pls.core.selectors.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxScriptExpressionElementReferenceProvider : PsiReferenceProvider() {
	override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
		val gameType = selectGameType(element) ?: return PsiReference.EMPTY_ARRAY
		val configGroup = getCwtConfig(element.project).getValue(gameType)
		val text = element.text
		
		//尝试兼容可能包含参数的情况
		//if(text.isParameterAwareExpression()) return PsiReference.EMPTY_ARRAY
		
		val isKey = element is ParadoxScriptPropertyKey
		val configs = ParadoxCwtConfigHandler.getConfigs(element, !isKey, isKey)
		val config = configs.firstOrNull()
		if(config != null) {
			val textRange = when {
				element is ParadoxScriptBlock -> TextRange.create(0, 1) //left curly brace
				else -> TextRange.create(0, text.length) //whole text, including possible quotes
			}
			val configExpression = config.expression
			when(configExpression.type) {
				CwtDataType.Value, CwtDataType.ValueSet -> {
					if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
					//quoted -> only value set value name, no scope info
					if(text.isLeftQuoted()) {
						val reference = ParadoxScriptExpressionPsiReference(element, textRange, config, isKey)
						return arrayOf(reference)
					}
					val valueFieldExpression = ParadoxValueSetValueExpression.resolve(text, textRange, config, configGroup, isKey)
					if(valueFieldExpression == null) return PsiReference.EMPTY_ARRAY
					return valueFieldExpression.getReferences(element)
				}
				CwtDataType.Scope, CwtDataType.ScopeField, CwtDataType.ScopeGroup -> {
					if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
					if(text.isLeftQuoted()) return PsiReference.EMPTY_ARRAY
					val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup, isKey)
					if(scopeFieldExpression == null) return PsiReference.EMPTY_ARRAY
					return scopeFieldExpression.getReferences(element)
				}
				CwtDataType.ValueField, CwtDataType.IntValueField -> {
					if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
					if(text.isLeftQuoted()) return PsiReference.EMPTY_ARRAY
					val valueFieldExpression = ParadoxValueFieldExpression.resolve(text, textRange, configGroup, isKey)
					if(valueFieldExpression == null) return PsiReference.EMPTY_ARRAY
					return valueFieldExpression.getReferences(element)
				}
				CwtDataType.VariableField, CwtDataType.IntVariableField -> {
					if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
					if(text.isLeftQuoted()) return PsiReference.EMPTY_ARRAY
					val variableFieldExpression = ParadoxVariableFieldExpression.resolve(text, textRange, configGroup, isKey)
					if(variableFieldExpression == null) return PsiReference.EMPTY_ARRAY
					return variableFieldExpression.getReferences(element)
				}
				else -> {
					if(element !is ParadoxScriptExpressionElement) return PsiReference.EMPTY_ARRAY
					val reference = ParadoxScriptExpressionPsiReference(element, textRange, config, isKey)
					return arrayOf(reference)
				}
			}
		}
		return PsiReference.EMPTY_ARRAY
	}
}
