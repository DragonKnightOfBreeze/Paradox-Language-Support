package icu.windea.pls.script.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.handler.ParadoxCwtConfigHandler.resolveConfigs
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

class ParadoxScriptExpressionElementReferenceProvider : PsiReferenceProvider() {
	override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
		if(element !is ParadoxScriptExpressionElement) return PsiReference.EMPTY_ARRAY
		val gameType = ParadoxSelectorUtils.selectGameType(element) ?: return PsiReference.EMPTY_ARRAY
		val configGroup = getCwtConfig(element.project).getValue(gameType)
		val text = element.text
		val isKey = element is ParadoxScriptPropertyKey
		
		//尝试兼容可能包含参数的情况
		//if(text.isParameterAwareExpression()) return PsiReference.EMPTY_ARRAY
		
		val config = resolveConfigs(element).firstOrNull()
		if(config != null) {
			val textRange = TextRange.create(0, text.length)
			when(config.expression.type) {
				CwtDataTypes.Value, CwtDataTypes.ValueSet -> {
					if(text.isQuoted()) return PsiReference.EMPTY_ARRAY
					val valueFieldExpression = ParadoxValueSetValueExpression.resolve(text, textRange, config.expression, configGroup, isKey)
					if(valueFieldExpression == null) return PsiReference.EMPTY_ARRAY
					return valueFieldExpression.getReferences(element)
				}
				CwtDataTypes.Scope, CwtDataTypes.ScopeField, CwtDataTypes.ScopeGroup -> {
					if(text.isQuoted()) return PsiReference.EMPTY_ARRAY
					val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup, isKey)
					if(scopeFieldExpression == null) return PsiReference.EMPTY_ARRAY
					return scopeFieldExpression.getReferences(element)
				}
				CwtDataTypes.ValueField, CwtDataTypes.IntValueField -> {
					if(text.isQuoted()) return PsiReference.EMPTY_ARRAY
					val valueFieldExpression = ParadoxValueFieldExpression.resolve(text, textRange, configGroup, isKey)
					if(valueFieldExpression == null) return PsiReference.EMPTY_ARRAY
					return valueFieldExpression.getReferences(element)
				}
				else -> {
					//TODO 不能直接返回PsiReference，需要先确定rangeInElement
					val reference = ParadoxScriptExpressionReference(element, textRange, config, isKey)
					return arrayOf(reference)
				}
			}
		}
		return PsiReference.EMPTY_ARRAY
	}
}
