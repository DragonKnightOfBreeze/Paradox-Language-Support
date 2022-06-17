package icu.windea.pls.script.reference

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.impl.*

/**
 * @see ParadoxScriptValueReference
 * @see ParadoxScriptLinkValuePrefixReference
 * @see ParadoxScriptLinkValueReference
 */
class ParadoxScriptValueExpressionReferenceProvider: PsiReferenceProvider(){
	override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
		if(element !is ParadoxScriptString) return PsiReference.EMPTY_ARRAY
		val expressionInfo = element.kvExpressionInfo ?: return PsiReference.EMPTY_ARRAY
		return when(expressionInfo.type){
			ParadoxKvExpressionType.LiteralType -> arrayOf(ParadoxScriptValueReference(element, expressionInfo.wholeRange))
			ParadoxKvExpressionType.ParameterType -> PsiReference.EMPTY_ARRAY
			ParadoxKvExpressionType.StringTemplateType -> PsiReference.EMPTY_ARRAY
			ParadoxKvExpressionType.ScopeExpression -> expressionInfo.ranges.mapToArray { ParadoxScriptLinkReference(element, it) }
			ParadoxKvExpressionType.ScopeValueExpression -> PsiReference.EMPTY_ARRAY //TODO
			ParadoxKvExpressionType.ScriptValueExpression -> PsiReference.EMPTY_ARRAY //unexpected
		}
	}
}