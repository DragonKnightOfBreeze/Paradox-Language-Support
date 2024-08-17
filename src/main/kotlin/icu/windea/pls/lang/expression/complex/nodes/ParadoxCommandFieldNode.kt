package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*

sealed interface ParadoxCommandFieldNode : ParadoxComplexExpressionNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxCommandFieldNode {
            ParadoxParameterizedCommandFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxPredefinedCommandFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicCommandFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorCommandFieldNode(text, textRange, configGroup)
        }
    }
}
