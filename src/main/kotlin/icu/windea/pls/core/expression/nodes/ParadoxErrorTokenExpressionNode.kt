package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*

class ParadoxErrorTokenExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange
) : ParadoxErrorExpressionNode
