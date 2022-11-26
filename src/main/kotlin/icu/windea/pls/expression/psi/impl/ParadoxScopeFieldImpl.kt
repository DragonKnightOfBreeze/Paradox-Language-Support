// This is a generated file. Not intended for manual editing.
package icu.windea.pls.expression.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.expression.psi.*

abstract class ParadoxScopeFieldImpl(node: ASTNode) : ASTWrapperPsiElement(node), ParadoxScopeField {
	open fun accept(visitor: ParadoxExpressionVisitor) {
		visitor.visitScopeField(this)
	}
	
	override fun accept(visitor: PsiElementVisitor) {
		if(visitor is ParadoxExpressionVisitor) accept(visitor) else super.accept(visitor)
	}
}
