package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.*

/**
 * 参见：`99_README_GRAMMAR.txt`
 */
class ParadoxParameterizedCommandFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandFieldNode, ParadoxParameterizedNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxParameterizedCommandFieldNode? {
            if(!text.isParameterized()) return null
            return ParadoxParameterizedCommandFieldNode(text, textRange, configGroup)
        }
    }
}
