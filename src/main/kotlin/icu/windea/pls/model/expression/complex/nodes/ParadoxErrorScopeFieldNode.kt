package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.psi.*

class ParadoxErrorScopeFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange
) : ParadoxComplexExpressionNode.Base(), ParadoxScopeFieldNode, ParadoxErrorNode {
    override fun getUnresolvedError(element: ParadoxScriptStringExpressionElement): ParadoxComplexExpressionError? {
        if(nodes.isNotEmpty()) return null
        if(text.isEmpty()) return null
        if(text.isParameterized()) return null
        return ParadoxComplexExpressionErrors.unresolvedScopeField(rangeInExpression, text)
    }
}
