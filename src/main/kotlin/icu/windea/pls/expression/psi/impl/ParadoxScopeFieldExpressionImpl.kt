// This is a generated file. Not intended for manual editing.
package icu.windea.pls.expression.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.expression.psi.*

class ParadoxScopeFieldExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), ParadoxScopeFieldExpression {
	fun accept(visitor: ParadoxExpressionVisitor) {
		visitor.visitScopeFieldExpression(this)
	}
	
	override fun accept(visitor: PsiElementVisitor) {
		if(visitor is ParadoxExpressionVisitor) accept(visitor) else super.accept(visitor)
	}
	
	override val scopeFieldList: List<ParadoxScopeField?>
		get() = PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScopeField::class.java)
}
