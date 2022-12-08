package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.script.psi.*

class ParadoxErrorValueFieldExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange
) : ParadoxValueFieldExpressionNode, ParadoxErrorExpressionNode {
	override fun getUnresolvedError(element: ParadoxScriptStringExpressionElement): ParadoxExpressionError? {
		if(nodes.isNotEmpty()) return null
		if(text.isEmpty()) return null
		if(text.isParameterAwareExpression()) return null
		return ParadoxUnresolvedScopeExpressionError(rangeInExpression, PlsBundle.message("script.expression.unresolvedValueField", text))
	}
}
