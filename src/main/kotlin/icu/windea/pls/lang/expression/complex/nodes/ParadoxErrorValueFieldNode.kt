package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.*

class ParadoxErrorValueFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNode.Base(), ParadoxValueFieldNode, ParadoxErrorNode {
    override fun getUnresolvedError(): ParadoxComplexExpressionError? {
        if (nodes.isNotEmpty()) return null
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        return ParadoxComplexExpressionError.Builder.unresolvedValueField(rangeInExpression, text)
    }
}
