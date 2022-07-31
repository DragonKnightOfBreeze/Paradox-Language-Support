package icu.windea.pls.script.expression.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class ParadoxScriptScopeFieldPrefixReference(
	element: ParadoxScriptExpressionElement,
	rangeInElement: TextRange,
	private val resolved: List<PsiElement>?
): PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement){
	override fun handleElementRename(newElementName: String): ParadoxScriptExpressionElement {
		throw IncorrectOperationException() //不允许重命名
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		return resolved?.mapToArray { PsiElementResolveResult(it) } ?: ResolveResult.EMPTY_ARRAY
	}
}

