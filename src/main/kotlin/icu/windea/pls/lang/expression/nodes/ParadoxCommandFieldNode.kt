package icu.windea.pls.lang.expression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup

sealed interface ParadoxCommandFieldNode : ParadoxComplexExpressionNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxCommandFieldNode {
            ParadoxPredefinedCommandFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicCommandFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxParameterizedCommandFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorCommandFieldNode(text, textRange, configGroup)
        }
    }
}
