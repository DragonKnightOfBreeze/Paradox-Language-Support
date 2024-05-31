package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.psi.*

class ParadoxErrorValueFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange
) : ParadoxValueFieldNode, ParadoxErrorNode {
    override fun getUnresolvedError(element: ParadoxScriptStringExpressionElement): ParadoxComplexExpressionError? {
        if(nodes.isNotEmpty()) return null
        if(text.isEmpty()) return null
        if(text.isParameterized()) return null
        return ParadoxComplexExpressionErrors.unresolvedValueField(rangeInExpression, text)
    }
}
