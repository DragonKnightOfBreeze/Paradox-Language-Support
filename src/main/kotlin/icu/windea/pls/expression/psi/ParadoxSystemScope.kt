// This is a generated file. Not intended for manual editing.
package icu.windea.pls.expression.psi

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.psi.*

interface ParadoxSystemScope : ParadoxScopeField, ParadoxExpressionNode {
	fun getName(): String
	val config: ParadoxSystemScopeConfig
	
	class Reference(
		element: ParadoxScriptExpressionElement,
		rangeInElement: TextRange,
		val resolved: CwtProperty?
	) : PsiReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement) {
		override fun handleElementRename(newElementName: String): ParadoxScriptExpressionElement {
			throw IncorrectOperationException() //不允许重命名
		}
		
		override fun resolve() = resolved
	}
}
