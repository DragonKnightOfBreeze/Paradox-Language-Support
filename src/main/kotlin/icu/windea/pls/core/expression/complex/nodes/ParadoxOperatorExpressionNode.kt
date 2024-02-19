package icu.windea.pls.core.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.script.highlighter.*

class ParadoxOperatorExpressionNode(
    override val text: String,
    override val rangeInExpression: TextRange
) : ParadoxTokenExpressionNode {
    override fun getAttributesKey() = ParadoxScriptAttributesKeys.OPERATOR_KEY
}
