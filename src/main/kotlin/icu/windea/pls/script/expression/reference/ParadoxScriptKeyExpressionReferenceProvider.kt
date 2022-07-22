package icu.windea.pls.script.expression.reference

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.expression.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.impl.*

/**
 * @see ParadoxScriptKeyReference
 * @see ParadoxScriptLinkReference
 */
class ParadoxScriptKeyExpressionReferenceProvider : PsiReferenceProvider() {
	override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
		if(element !is ParadoxScriptPropertyKey) return PsiReference.EMPTY_ARRAY
		val expressionInfo = element.expressionInfo ?: return PsiReference.EMPTY_ARRAY
		return when(expressionInfo.type){
			ParadoxKvExpressionType.LiteralType -> arrayOf(ParadoxScriptKeyReference(element, expressionInfo.wholeRange))
			ParadoxKvExpressionType.ParameterType -> PsiReference.EMPTY_ARRAY
			ParadoxKvExpressionType.StringTemplateType -> PsiReference.EMPTY_ARRAY
			ParadoxKvExpressionType.ScopeExpression -> expressionInfo.ranges.mapToArray { ParadoxScriptLinkReference(element, it) }
			ParadoxKvExpressionType.ScopeValueExpression -> PsiReference.EMPTY_ARRAY //TODO
			ParadoxKvExpressionType.ScriptValueExpression -> PsiReference.EMPTY_ARRAY
		}
	}
}