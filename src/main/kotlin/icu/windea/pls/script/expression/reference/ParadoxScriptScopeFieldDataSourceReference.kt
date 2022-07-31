package icu.windea.pls.script.expression.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.script.psi.*

class ParadoxScriptScopeFieldDataSourceReference(
	element: ParadoxScriptExpressionElement,
	rangeInElement: TextRange,
	private val dataSources: List<CwtValueExpression>
) : PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): ParadoxScriptExpressionElement {
		throw IncorrectOperationException() //不允许重命名（尽管有些类型如定义实际上可以重命名）
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		val element = element
		return dataSources.mapNotNull { dataSource ->
			val resolved = CwtConfigHandler.resolveScriptExpression(element, null, rangeInElement) ?: return@mapNotNull null
			ParadoxScriptScopeFieldDataSourceResolveResult(resolved, true, dataSource)
		}.toTypedArray()
	}
}

class ParadoxScriptScopeFieldDataSourceResolveResult(
	element: PsiElement,
	validResult: Boolean = true,
	val expression: CwtValueExpression
) : PsiElementResolveResult(element, validResult)