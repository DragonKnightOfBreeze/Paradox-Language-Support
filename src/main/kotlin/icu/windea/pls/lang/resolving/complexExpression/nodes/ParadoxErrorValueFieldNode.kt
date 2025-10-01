package icu.windea.pls.lang.resolving.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.resolving.complexExpression.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolving.complexExpression.ParadoxComplexExpressionErrorBuilder

class ParadoxErrorValueFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNodeBase(), ParadoxValueFieldNode, ParadoxErrorNode {
    override fun getUnresolvedError(): ParadoxComplexExpressionError? {
        if (nodes.isNotEmpty()) return null
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        return ParadoxComplexExpressionErrorBuilder.unresolvedValueField(rangeInExpression, text)
    }
}
