// This is a generated file. Not intended for manual editing.
package icu.windea.pls.expression.psi.impl

import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.core.*
import icu.windea.pls.expression.psi.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxSystemScopeImpl(node: ASTNode) : ParadoxScopeFieldImpl(node), ParadoxSystemScope {
	override fun accept(visitor: ParadoxExpressionVisitor) {
		visitor.visitSystemScope(this)
	}
	
	override fun accept(visitor: PsiElementVisitor) {
		if(visitor is ParadoxExpressionVisitor) accept(visitor) else super.accept(visitor)
	}
	
	@Volatile var _name: String? = null
	@Volatile var _config: ParadoxSystemScopeConfig? = null
	
	override fun getName() = _name ?: text.also { _name = it }
	
	override val config get() = _config ?: getInternalConfig(project).systemScopeMap.getValue(name)
	
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.SYSTEM_SCOPE_KEY
	
	override fun getReference(element: ParadoxScriptExpressionElement): ParadoxSystemScope.Reference {
		return ParadoxSystemScope.Reference(element, textRange, config.pointer.element)
	}
}
