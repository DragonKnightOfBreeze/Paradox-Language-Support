package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.isParameterized

/**
 * 参见：`99_README_GRAMMAR.txt`
 */
class ParadoxParameterizedCommandFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNodeBase(), ParadoxCommandFieldNode, ParadoxParameterizedNode {
    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxParameterizedCommandFieldNode? {
            if (!text.isParameterized()) return null
            return ParadoxParameterizedCommandFieldNode(text, textRange, configGroup)
        }
    }

    companion object : Resolver()
}
