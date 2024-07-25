package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*

sealed interface ParadoxCommandFieldLinkNode : ParadoxComplexExpressionNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxCommandFieldLinkNode {
            ParadoxParameterizedCommandFieldLinkNode.resolve(text, textRange)?.let { return it }
            ParadoxPredefinedCommandFieldLinkNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicCommandFieldLinkNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorCommandFieldLinkNode(text, textRange)
        }
    }
}
