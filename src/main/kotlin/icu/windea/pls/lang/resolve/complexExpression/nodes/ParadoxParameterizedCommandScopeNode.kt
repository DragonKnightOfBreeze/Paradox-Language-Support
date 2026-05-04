package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.isParameterized

class ParadoxParameterizedCommandScopeNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNodeBase(), ParadoxCommandScopeNode, ParadoxParameterizedNode {
    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxParameterizedCommandScopeNode? {
            if (!text.isParameterized()) return null
            return ParadoxParameterizedCommandScopeNode(text, textRange, configGroup)
        }
    }

    companion object : Resolver()
}
