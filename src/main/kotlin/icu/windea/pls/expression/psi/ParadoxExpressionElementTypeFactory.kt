package icu.windea.pls.expression.psi

import com.intellij.psi.tree.*

object ParadoxExpressionElementTypeFactory {
	@JvmStatic fun getTokenType(debugName: String): IElementType {
		return ParadoxExpressionTokenType(debugName)
	}
	
	@JvmStatic fun getElementType(debugName: String): IElementType {
		return ParadoxExpressionElementType(debugName)
	}
}