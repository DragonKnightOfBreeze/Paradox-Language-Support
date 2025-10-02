package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.isParameterized

class ParadoxParameterizedCommandScopeLinkNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNodeBase(), ParadoxCommandScopeLinkNode, ParadoxParameterizedNode {
    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxParameterizedCommandScopeLinkNode? {
            if (!text.isParameterized()) return null
            return ParadoxParameterizedCommandScopeLinkNode(text, textRange, configGroup)
        }
    }

    companion object : Resolver()
}
