package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.lang.*

class ParadoxParameterizedCommandFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange,
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandFieldNode, ParadoxParameterizedNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange): ParadoxParameterizedCommandFieldNode? {
            if(!text.isParameterized()) return null
            return ParadoxParameterizedCommandFieldNode(text, textRange)
        }
    }
}
