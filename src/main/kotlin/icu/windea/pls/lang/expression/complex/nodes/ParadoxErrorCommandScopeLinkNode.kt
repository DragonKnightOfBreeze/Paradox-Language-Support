package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.expression.complex.*

class ParadoxErrorCommandScopeLinkNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandScopeLinkNode, ParadoxErrorNode {
    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if(nodes.isNotEmpty()) return null
        if(text.isEmpty()) return null
        if(text.isParameterized()) return null
        return ParadoxComplexExpressionErrors.unresolvedCommandScope(rangeInExpression, text)
    }
}
