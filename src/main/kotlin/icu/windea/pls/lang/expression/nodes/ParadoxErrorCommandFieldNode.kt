package icu.windea.pls.lang.expression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.expression.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.expression.ParadoxComplexExpressionError
import icu.windea.pls.lang.isParameterized

class ParadoxErrorCommandFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandFieldNode, ParadoxErrorNode {
    override fun getUnresolvedError(): ParadoxComplexExpressionError? {
        if (nodes.isNotEmpty()) return null
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        return ParadoxComplexExpressionErrorBuilder.unresolvedCommandField(rangeInExpression, text)
    }
}
