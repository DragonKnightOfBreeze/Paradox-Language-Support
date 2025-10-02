package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup

sealed interface ParadoxCommandFieldNode : ParadoxComplexExpressionNode {
    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxCommandFieldNode {
            ParadoxPredefinedCommandFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicCommandFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxParameterizedCommandFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorCommandFieldNode(text, textRange, configGroup)
        }
    }

    companion object : Resolver()
}
