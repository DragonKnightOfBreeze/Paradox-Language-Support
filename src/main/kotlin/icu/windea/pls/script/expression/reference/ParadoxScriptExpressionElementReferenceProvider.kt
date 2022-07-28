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
		if(!text.isQuoted()) {
			val config = element.getConfig()
			if(config != null){
				when(config.expression.type){
					CwtDataTypes.Scope, CwtDataTypes.ScopeField, CwtDataTypes.ScopeGroup -> {
						val scopeLinkExpression = ParadoxScriptScopeExpression.resolve(text, configGroup)
						if(scopeLinkExpression.isEmpty()) return PsiReference.EMPTY_ARRAY
						return scopeLinkExpression.infos.mapToArray { it.getReference(element) }
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