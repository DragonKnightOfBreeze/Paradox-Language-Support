package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.script.highlighter.*

class ParadoxOperatorExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange
) : ParadoxScriptTokenExpressionNode {
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.OPERATOR_KEY
}
