package icu.windea.pls.script.expression

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.psi.*

sealed class ParadoxScriptExpressionInfo(
	val text: String,
	val textRange: TextRange,
	val directlyResolved: PsiElement? = null,
	val directlyResolvedList: List<PsiElement>? = null
) {
	open fun getReference(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): PsiReference? = null 
	
	open fun isUnresolved(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): Boolean {
		if(directlyResolved != null) return false
		val reference = getReference(element, config) ?: return false
		if(reference is PsiPolyVariantReference) return reference.multiResolve(false).isEmpty()
		return reference.resolve() == null
	}
	
	open fun getUnresolvedError(): ParadoxScriptExpressionError? = null
	
	open fun getAttributesKey(): TextAttributesKey? = null
	
	open fun getAttributesKeyExpressions(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): List<CwtDataExpression> = emptyList()
}