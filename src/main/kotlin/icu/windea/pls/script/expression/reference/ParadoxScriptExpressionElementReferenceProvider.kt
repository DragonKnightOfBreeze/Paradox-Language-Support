package icu.windea.pls.script.expression.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.script.expression.*
import icu.windea.pls.script.psi.*

/**
 * @see ParadoxScriptKeyReference
 * @see ParadoxScriptValueReference
 * @see ParadoxScriptScopeReference
 */
class ParadoxScriptExpressionElementReferenceProvider : PsiReferenceProvider() {
	override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
		if(element !is ParadoxScriptExpressionElement) return PsiReference.EMPTY_ARRAY
		val gameType = element.fileInfo?.gameType ?: return PsiReference.EMPTY_ARRAY
		val configGroup = getCwtConfig(element.project).getValue(gameType)
		val text  = element.text
		val textRange = TextRange.create(0, text.length)
		//排除可能包含参数的情况
		if(text.isParameterAwareExpression()) return PsiReference.EMPTY_ARRAY
		if(!text.isQuoted()) {
			val config = element.getConfig()
			if(config != null){
				when(config.expression.type){
					CwtDataTypes.Scope, CwtDataTypes.ScopeField, CwtDataTypes.ScopeGroup -> {
						val scopeFieldExpression = ParadoxScriptScopeFieldExpression.resolve(text, configGroup)
						if(scopeFieldExpression.isEmpty()) return PsiReference.EMPTY_ARRAY
						return scopeFieldExpression.infos.mapNotNull { it.getReference(element) }.toTypedArray()
					}
					CwtDataTypes.ValueField, CwtDataTypes.IntValueField -> {
						val valueFieldExpression = ParadoxScriptValueFieldExpression.resolve(text, configGroup)
						if(valueFieldExpression.isEmpty()) return PsiReference.EMPTY_ARRAY
						return valueFieldExpression.infos.mapNotNull { it.getReference(element) }.toTypedArray()
					}
					else -> pass() //TODO
				}
			}
		}
		return when{
			element is ParadoxScriptPropertyKey -> arrayOf(ParadoxScriptKeyReference(element, textRange))
			element is ParadoxScriptString -> arrayOf(ParadoxScriptValueReference(element, textRange))
			else -> throw InternalError()
		}
	}
}