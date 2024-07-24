package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.lang.*

class ParadoxParameterizedCommandScopeNode(
    override val text: String,
    override val rangeInExpression: TextRange,
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandScopeNode, ParadoxParameterizedNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange): ParadoxParameterizedCommandScopeNode? {
            if(!text.isParameterized()) return null
            return ParadoxParameterizedCommandScopeNode(text, textRange)
        }
    }
}
