package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.highlighter.*

class ParadoxParameterizedScopeExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
) : ParadoxScopeExpressionNode {
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.SCOPE_KEY
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange): ParadoxParameterizedScopeExpressionNode? {
			if(!text.isParameterized()) return null
			return ParadoxParameterizedScopeExpressionNode(text, textRange)
		}
	}
}
