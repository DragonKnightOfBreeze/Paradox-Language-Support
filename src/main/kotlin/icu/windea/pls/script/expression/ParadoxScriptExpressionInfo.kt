package icu.windea.pls.script.expression

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.script.psi.*

sealed class ParadoxScriptExpressionInfo(
	val text: String,
	val textRange: TextRange,
	val directlyResolved: PsiElement? = null,
	val directlyResolvedList: List<PsiElement>? = null
) {
	open fun getReference(element: ParadoxScriptExpressionElement, config: CwtKvConfig<*>): PsiReference? = null 
	
	open fun isUnresolved(element: ParadoxScriptExpressionElement, config: CwtKvConfig<*>): Boolean {
		if(directlyResolved != null) return false
		val reference = getReference(element, config) ?: return false
		if(reference is PsiPolyVariantReference) return reference.multiResolve(false).isEmpty()
		return reference.resolve() == null
	}
	
	open fun getUnresolvedError(): ParadoxScriptExpressionError? = null
	
	open fun getAttributesKey(): TextAttributesKey? = null
	
	open fun getAttributesKeyExpressions(element: ParadoxScriptExpressionElement, config: CwtKvConfig<*>): List<CwtKvExpression> = emptyList()
}