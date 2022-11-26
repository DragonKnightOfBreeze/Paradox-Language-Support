// This is a generated file. Not intended for manual editing.
package icu.windea.pls.expression.psi.impl

import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.expression.psi.*

class ParadoxScopeLinkImpl(node: ASTNode) : ParadoxScopeFieldImpl(node), ParadoxScopeLink {
	override fun accept(visitor: ParadoxExpressionVisitor) {
		visitor.visitScopeLink(this)
	}
	
	override fun accept(visitor: PsiElementVisitor) {
		if(visitor is ParadoxExpressionVisitor) accept(visitor) else super.accept(visitor)
	}
}
