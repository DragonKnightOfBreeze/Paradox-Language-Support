package icu.windea.pls.core.expression.complex.nodes

import com.intellij.openapi.util.*

class ParadoxErrorTokenExpressionNode(
    override val text: String,
    override val rangeInExpression: TextRange
) : ParadoxErrorExpressionNode
