package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.script.highlighter.*

class ParadoxOperatorNode(
    override val text: String,
    override val rangeInExpression: TextRange
) : ParadoxTokenNode {
    override fun getAttributesKey() = ParadoxScriptAttributesKeys.OPERATOR_KEY
}
