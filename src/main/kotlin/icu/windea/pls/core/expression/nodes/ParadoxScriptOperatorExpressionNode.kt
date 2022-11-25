package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.script.highlighter.*

class ParadoxScriptOperatorExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange
) : ParadoxScriptTokenExpressionNode {
	//TODO since 0.7.4: should erase original highlight first
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.OPERATOR_KEY
}
