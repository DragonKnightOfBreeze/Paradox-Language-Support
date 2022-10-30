package icu.windea.pls.script.expression.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.expression.*
import icu.windea.pls.script.psi.*

class ParadoxScriptExpressionElementReferenceProvider : PsiReferenceProvider() {
	override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
		if(element !is ParadoxScriptExpressionElement) return PsiReference.EMPTY_ARRAY
		val gameType = ParadoxSelectorUtils.selectGameType(element) ?: return PsiReference.EMPTY_ARRAY
		val configGroup = getCwtConfig(element.project).getValue(gameType)
		val text = element.text
		val textRange = TextRange.create(0, text.length)
		//排除可能包含参数的情况
		if(text.isParameterAwareExpression()) return PsiReference.EMPTY_ARRAY
		val config = ParadoxCwtConfigHandler.resolveConfig(element)
		if(config != null) {
			if(!text.isQuoted()) {
				when(config.expression.type) {
					CwtDataTypes.Scope, CwtDataTypes.ScopeField, CwtDataTypes.ScopeGroup -> {
						val scopeFieldExpression = ParadoxScriptExpression.resolveScopeField(text, configGroup)
						if(scopeFieldExpression.isEmpty()) return PsiReference.EMPTY_ARRAY
						return scopeFieldExpression.infos.mapNotNull { it.getReference(element, config) }.toTypedArray()
					}
					CwtDataTypes.ValueField, CwtDataTypes.IntValueField -> {
						val valueFieldExpression = ParadoxScriptExpression.resolveValueField(text, configGroup)
						if(valueFieldExpression.isEmpty()) return PsiReference.EMPTY_ARRAY
						return valueFieldExpression.infos.mapNotNull { it.getReference(element, config) }.toTypedArray()
					}
					CwtDataTypes.Value, CwtDataTypes.ValueSet -> {
						val valueSetValueExpression = ParadoxScriptExpression.resolveValueSetValue(text, configGroup)
						if(valueSetValueExpression.isEmpty()) return PsiReference.EMPTY_ARRAY
						return valueSetValueExpression.infos.mapNotNull { it.getReference(element, config) }.toTypedArray()
					}
					else -> pass() //TODO
				}
			}
			//TODO 不能直接返回PsiReference，需要先确定textRange
			return arrayOf(ParadoxScriptExpressionReference(element, textRange, config, element is ParadoxScriptPropertyKey))
		}
		return PsiReference.EMPTY_ARRAY
	}
}